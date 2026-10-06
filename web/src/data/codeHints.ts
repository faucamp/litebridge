export interface CodeHint {
  title: string;
  description: string;
  code: string;
  lang?: string;
  filename?: string;
  codeHtml?: string;
}

export type CodeHintRegistry = Record<string, CodeHint>;

export const defaultCodeHints: CodeHintRegistry = {
  Person: {
    title: 'Person.java',
    description: `Example DTO representing an individual.

Litebridge allows the use of <strong>raw/unaltered DTO classes</strong> as entities; these can be typical Java beans with getters/setters, records, or custom POJOs.

Database columns and relationships are mapped programmatically to the DTO using <a href="/mapping"><code>litebridge.register()</code></a>.`,
    code: `public class Person {
    private Long id;
    private String name;
    private String surname;
    private int age;
    private List<Account> accounts;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public List<Account> getAccounts() { return accounts; }
    public void setAccounts(List<Account> accounts) { this.accounts = accounts; }
}`,
    lang: 'java',
    filename: 'Person.java',
  },

  PersonMeta: {
    title: 'PersonMeta.java',
    description: 'Generated metamodel providing static query expressions for <strong>type-safe query formulation</strong>.',
    code: `public final class PersonMeta {
    public static final NumericQueryField id = new NumericQueryField(Person.class, "id");
    public static final StringQueryField name = new StringQueryField(Person.class, "name");
    public static final StringQueryField surname = new StringQueryField(Person.class, "surname");
    public static final NumericQueryField age = new NumericQueryField(Person.class, "age");
    public static final QueryField accounts = new QueryField(Person.class, "accounts");
}`,
    lang: 'java',
    filename: 'PersonMeta.java',
  },

  PersonEntity: {
    title: 'PersonEntity.java',
    description: 'Example entity class annotated with <code>@Table</code>, <code>@Column</code> and others, for <strong>declarative table mapping</strong>.',
    code: `@Table("LB.PERSON")
public class PersonEntity {
    @Column(value = "PERSON_ID", generateUsingSequence = "LB.PERSON_SEQ")
    private Long id;

    @Column("FIRST_NAME")
    private String name;

    @Column("SURNAME")
    private String surname;

    @OneToMany(mappedByField = "owner")
    private List<AccountEntity> accounts;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public List<AccountEntity> getAccounts() { return accounts; }
    public void setAccounts(List<AccountEntity> accounts) { this.accounts = accounts; }
}`,
    lang: 'java',
    filename: 'PersonEntity.java',
  },

  Account: {
    title: 'Account.java',
    description: 'Example DTO representing a financial account associated with a <code>Person</code> owner.',
    code: `public class Account {
    private Long id;
    private String accountNumber;
    private BigDecimal balance;
    private Person owner;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public Person getOwner() { return owner; }
    public void setOwner(Person owner) { this.owner = owner; }
}`,
    lang: 'java',
    filename: 'Account.java',
  },

  AccountMeta: {
    title: 'AccountMeta.java',
    description: 'Generated metamodel providing static query fields for <code>Account</code> queries.',
    code: `public final class AccountMeta {
    public static final NumericQueryField id = new NumericQueryField(Account.class, "id");
    public static final StringQueryField accountNumber = new StringQueryField(Account.class, "accountNumber");
    public static final NumericQueryField balance = new NumericQueryField(Account.class, "balance");
    public static final QueryField owner = new QueryField(Account.class, "owner");
}`,
    lang: 'java',
    filename: 'AccountMeta.java',
  },

  Litebridge: {
    title: 'Litebridge.java',
    description: `The core entry point for fluent database operations (<code>select</code>, <code>insert</code>, <code>update</code>, <code>delete</code>, <code>merge</code>).

Typically instantiated using the <code>Litebridge.withDatabase()</code> builder method, or injected if using Spring.

Different database providers change the capabilities of Litebridge by extending or constraining its API:
`,
    code: `import org.litebridge.orm.Litebridge;
import org.litebridge.db.h2.H2DatabaseProvider;
import org.litebridge.db.h2.OracleDatabseProvider;
import org.litebridge.db.postgres.PostgresDatabaseProvider;
import org.litebridge.db.sqlite.SQLiteDatabaseProvider;

// For H2
Litebridge litebridge = Litebridge
    .withDatabaseProvider(
        new H2DatabaseProvider(), dataSource)
    .build();
    
// Oracle provides database-specific extensions to the 
// base Litebridge interface, such as "insertAll()"
LitebridgeOracle litebridge = Litebridge
    .withDatabaseProvider(
        new OracleDatabaseProvider(), dataSource)
    .build();

// For PostgreSQL
Litebridge litebridge = Litebridge
    .withDatabaseProvider(
        new PostgresDatabaseProvider(), dataSource)
    .build();

// SQLite uses LitebridgeCore as it does not have "merge" functionality
LitebridgeCore litebridge = Litebridge
    .withDatabaseProvider(
        new SQLiteDatabaseProvider(), dataSource)
    .build();
    `,
    lang: 'java',
    filename: 'Litebridge.java',
  },

  Row: {
    title: 'Row.java',
    description: 'Litebridge representation of an untyped database row containing key-value column pairs.',
    code: `public final class Row {

    /**
     * Returns the list of columns in this row.
     */
    List<RowColumn> columns();

    /**
     * Provides a map of column labels to columns for the current row.
     */
    public Map<String, RowColumn> columnMap();

    /**
     * Retrieves the value of the column at the given index.
     */
    public @Nullable Object value(final int index);

    /**
     * Retrieves the value of the column with the given label/alias.
     */
    public @Nullable Object value(final String label);

    /**
     * Retrieves the column at the given index.
     */
    public RowColumn column(final int index);

    /**
     * Retrieves the column with the given label/alias.
     */
    public RowColumn column(final String label);

    /**
     * Retrieves the index of a column from the row by its <code>Column</code> metadata.
     */
    public int indexOf(final Column column);

    /**
     * Retrieves the index of a column from the row by its label/alias.
     */
    public int indexOf(final String label);

    /**
     * Returns the number of columns in this row.
     */
    public int size();
}
`,
    lang: 'java',
    filename: 'Row.java',
  },

  H2DatabaseProvider: {
    title: 'H2DatabaseProvider.java',
    description: 'DatabaseProvider implementation providing SQL dialect and metadata support for H2.',
    code: `public final class H2DatabaseProvider extends AbstractDatabaseProvider {
// Database provider implementation
}`,
    lang: 'java',
    filename: 'H2DatabaseProvider.java',
  },

  OracleDatabaseProvider: {
    title: 'OracleDatabaseProvider.java',
    description: 'DatabaseProvider implementation providing SQL dialect and metadata support for Oracle.',
    code: `public final class OracleDatabaseProvider extends AbstractDatabaseProvider {
// Database provider implementation
}`,
    lang: 'java',
    filename: 'H2DatabaseProvider.java',
  },

  'Fn': {
    title: 'Fn.java',
    description: 'Collection of static query expressions used to invoke SQL functions, do type conversions, etc, e.g. <code>Fn.count()</code>. ',
    code: `public final class Fn {

    /**
     * {@code COUNT()}: Selects the count of rows matching the query.
     *
     * @return a {@link CountSpec} expression instance to select the count of rows.
     */
    public static TypeOverrideExpressionSpec<Long> count();
    
    // Several other static query expressions
}`,
    lang: 'java',
    filename: 'H2DatabaseProvider.java',
  },

  'upper': {
    title: 'Fn.java',
    description: 'Query expression that returns the uppercase value of a column\'s text.',
    code: `/**
 * {@code UPPER()}: Returns the uppercase value of a column's text.
 *
 * @param column Target column/field name
 * @return a {@link ProtoNestableTOExpr} expression instance to select a specific column.
 */
public static ProtoNestableTOExpr<String> upper(final String column);`,
    lang: 'java',
    filename: 'H2DatabaseProvider.java',
  },

  'surname.upper()': {
    title: 'Fn.java',
    description: 'Metamodel field expression that returns the uppercase value of a column\'s text.',
    code: `public final class StringQueryField extends QueryField {

    /**
     * {@code UPPER()}: Returns the uppercase value of a column's text.
     *
     * @return a {@link ProtoNestableTOExpr} expression instance to select a specific column.
     */
    public StringQueryField upper() {
        return new StringQueryField(this,
                Fn.upper(Objects.requireNonNullElseGet(pendingExpressionSpec, () -> Fn.f(dtoClass, field))));
    }

    /**
     * {@code LOWER()}: Returns the lowercase value of a column's text.
     *
     * @return a {@link ProtoNestableTOExpr} expression instance to select a specific column.
     */
    public StringQueryField lower() {
        return new StringQueryField(this,
                Fn.lower(Objects.requireNonNullElseGet(pendingExpressionSpec, () -> Fn.f(dtoClass, field))));
    }
}`,
    lang: 'java',
    filename: 'H2DatabaseProvider.java',
  },
};
