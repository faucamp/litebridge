package org.litebridge.orm.engine.compiler;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.DeleteNode;
import org.litebridge.orm.engine.ast.InsertNode;
import org.litebridge.orm.engine.ast.MergeNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.SelectNode;
import org.litebridge.orm.engine.ast.UpdateNode;

import java.util.List;

/**
 * Compiler for AST query nodes.
 * <p>
 * The compiler processes query node chains and producing {@link PreparedOperation} instances from them.
 * <p>
 * This class acts as a root-level query compiler that delegates responsibility to
 * specialised compilers for different query types.
 * <p>
 * Specialised compilers are lazily initialised and maintained as
 * private fields to ensure their reuse and improve performance.
 * <p>
 * This class is thread-safe.
 */
public final class QueryCompiler extends AbstractRootQueryCompiler {

    private @Nullable SelectQueryCompiler selectQueryCompiler;
    private @Nullable InsertQueryCompiler insertQueryCompiler;
    private @Nullable UpdateQueryCompiler updateQueryCompiler;
    private @Nullable MergeQueryCompiler mergeQueryCompiler;
    private @Nullable DeleteQueryCompiler deleteQueryCompiler;

    /**
     * Constructs a new QueryCompiler instance.
     *
     * @param litebridgeContext Current Litebridge context.
     */
    public QueryCompiler(final LitebridgeContext litebridgeContext) {
        super(litebridgeContext);
    }

    /**
     * Compiles a given query node into a prepared operation.
     * <p>
     * This method resolves the appropriate query compiler for the root query node type
     * and applies all nodes in the query chain to create a structured database operation
     * along with its associated bind values.
     *
     * @param node the query node to be compiled
     * @return a {@code PreparedOperation} containing the structured operation and bind values
     * @throws IllegalArgumentException if the root query node type is unsupported
     */
    public PreparedOperation compile(final QueryNode node) {
        final List<QueryNode> nodes = flatten(node);

        final AbstractQueryCompiler<?> compiler = switch (nodes.getFirst()) {
            case SelectNode selectNode -> ensureSelectQueryCompiler();
            case InsertNode insertNode -> ensureInsertQueryCompiler();
            case UpdateNode updateNode -> ensureUpdateQueryCompiler();
            case MergeNode mergeNode -> ensureMergeQueryCompiler();
            case DeleteNode deleteNode -> ensureDeleteQueryCompiler();
            default -> throw new IllegalArgumentException("Unsupported root query node type: " + nodes.getFirst());
        };

        final CompilationContext compilationContext = compiler.createCompilationContext(nodes.getFirst());
        compiler.applyNodes(nodes, compilationContext);

        final Operation operation = compilationContext.toOperation();
        final List<BindValue> bindValues = compilationContext.getBindValues();
        return new PreparedOperation(operation, bindValues);
    }

    private SelectQueryCompiler ensureSelectQueryCompiler() {
        if (selectQueryCompiler == null) {
            selectQueryCompiler = new SelectQueryCompiler(litebridgeContext);
        }

        return selectQueryCompiler;
    }

    private InsertQueryCompiler ensureInsertQueryCompiler() {
        if (insertQueryCompiler == null) {
            insertQueryCompiler = new InsertQueryCompiler(litebridgeContext);
        }

        return insertQueryCompiler;
    }

    private UpdateQueryCompiler ensureUpdateQueryCompiler() {
        if (updateQueryCompiler == null) {
            updateQueryCompiler = new UpdateQueryCompiler(litebridgeContext);
        }

        return updateQueryCompiler;
    }

    private MergeQueryCompiler ensureMergeQueryCompiler() {
        if (mergeQueryCompiler == null) {
            mergeQueryCompiler = new MergeQueryCompiler(litebridgeContext);
        }

        return mergeQueryCompiler;
    }

    private DeleteQueryCompiler ensureDeleteQueryCompiler() {
        if (deleteQueryCompiler == null) {
            deleteQueryCompiler = new DeleteQueryCompiler(litebridgeContext);
        }

        return deleteQueryCompiler;
    }
}
