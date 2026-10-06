export interface ParsedCode {
  imports: string | null;
  body: string;
  hasImports: boolean;
  detectedHintKeys: string[];
}

/**
 * Splits a code string into import statements and main code body.
 * Captures leading package/import statements and comments attached to imports.
 */
export function splitImportsAndBody(code: string): { imports: string | null; body: string; hasImports: boolean } {
  const lines = code.split('\n');

  let lastImportLineIdx = -1;
  let hasImport = false;

  for (let i = 0; i < lines.length; i++) {
    const trimmed = lines[i].trim();
    if (trimmed.startsWith('import ') || trimmed.startsWith('import\t')) {
      hasImport = true;
      lastImportLineIdx = i;
    } else if (hasImport && trimmed.length > 0 && !trimmed.startsWith('//') && !trimmed.startsWith('/*') && !trimmed.startsWith('*')) {
      // We found the start of body code after previous imports
      break;
    }
  }

  if (!hasImport || lastImportLineIdx === -1) {
    return {
      imports: null,
      body: code.trimEnd(),
      hasImports: false,
    };
  }

  const importLines = lines.slice(0, lastImportLineIdx + 1);
  let bodyLines = lines.slice(lastImportLineIdx + 1);

  // Remove leading blank lines from body
  while (bodyLines.length > 0 && bodyLines[0].trim() === '') {
    bodyLines.shift();
  }

  return {
    imports: importLines.join('\n').trim(),
    body: bodyLines.join('\n'),
    hasImports: true,
  };
}

/**
 * Strips comments (lines starting with "//") and literal strings from code.
 */
export function stripCommentsAndStrings(code: string): string {
  if (!code) return '';

  // 1. Remove text blocks / multiline strings
  let cleaned = code.replace(/"""[\s\S]*?"""/g, '""');

  // 2. Remove double-quoted string literals
  cleaned = cleaned.replace(/"(?:[^"\\]|\\.)*"/g, '""');

  // 3. Remove single-quoted string/char literals
  cleaned = cleaned.replace(/'(?:[^'\\]|\\.)*'/g, "''");

  // 4. Remove comment lines starting with // and trailing comments
  const lines = cleaned.split('\n');
  const nonCommentLines = lines.map((line) => {
    const trimmed = line.trim();
    if (trimmed.startsWith('//')) {
      return '';
    }
    const commentIdx = line.indexOf('//');
    if (commentIdx !== -1) {
      return line.substring(0, commentIdx);
    }
    return line;
  });

  return nonCommentLines.join('\n');
}

/**
 * Identifies which hint keys are present in the given code string,
 * excluding occurrences within comments (lines starting with //) and string literals.
 */
export function detectHintKeys(code: string, availableHintKeys: string[]): string[] {
  if (!code || availableHintKeys.length === 0) return [];

  const cleanedCode = stripCommentsAndStrings(code);

  const found: string[] = [];
  for (const key of availableHintKeys) {
    // Word boundary match
    const regex = new RegExp(`\\b${key}\\b`);
    if (regex.test(cleanedCode)) {
      found.push(key);
    }
  }

  return found;
}

/**
 * Parses a code snippet, extracting imports and identifying active hints.
 */
export function parseCodeSnippet(code: string, availableHintKeys: string[] = []): ParsedCode {
  const { imports, body, hasImports } = splitImportsAndBody(code);
  const detectedHintKeys = detectHintKeys(code, availableHintKeys);

  return {
    imports,
    body,
    hasImports,
    detectedHintKeys,
  };
}
