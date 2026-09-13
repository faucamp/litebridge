# Litebridge Architecture

## Component diagram

A high-level component diagram of Litebridge is provided below:

```mermaid
flowchart TB
    subgraph ApiGroup [API]
        LitebrideBuilder[Litebridge Builder]
        
        subgraph LitebridgeCoreGroup [LitebridgeCore]
            RegistrationApi[Registration API]
            SelectApi[Select API]
            InsertApi[Insert API]
            UpdateApi[Update API]
            DeleteApi[Delete API]
            NativeSqlApi[Native SQL API]
            TransactionsApi[Transactions API]
        end
        style LitebridgeCoreGroup fill:#f9f9e6

        subgraph LitebridgeGroup [Litebridge]
            MergeApi[Merge API]
        end
        style LitebridgeGroup fill:#f9f9e6
    end

    subgraph CoreOrmGroup [Core ORM]
        TransactionManager[Transaction Manager]
        subgraph OrmMetadata [ORM Metadata]
            TableRegistry[Table Registry]
        end
        style OrmMetadata fill:#f9f9e6
        
        subgraph PersistenceGroup [Persistence]
            ChangeTracker[Change Tracker]
            PersistenceFacade[Persistence Facade] 
            InsertStatementBuilder[Insert Statement Builder]
            UpdateStatementBuilder[Update Statement Builder]
            DeleteStatementBuilder[Delete Statement Builder]
        end
        style PersistenceGroup fill:#f9f9e6
        
        subgraph EngineGroup [Statement Engines]
            RegistrationEngine[Registration Engine]
            SelectEngine[Select Engine]
            InsertEngine[Insert Engine]
            MergeEngine[Merge Engine]
            UpdateEngine[Update Engine]
            DeleteEngine[Delete Engine]
        end
        style EngineGroup fill:#f9f9e6
        
        subgraph NativeSqlGroup [Native SQL]
            NativeSqlEngine[Native SQL Engine]
            NativeSqlCache[Native SQL Cache]
        end
        style NativeSqlGroup fill:#f9f9e6
        
        subgraph QueryCompilerGroup [AST Compiler]
            QueryCompiler[Query Compiler]
            QueryPlanCache[Query Plan Cache]
        end
        style QueryCompilerGroup fill:#f9f9e6
    end

    subgraph DatabaseProvider [Database Provider]
        SqlGenerator[SQL Generator]
        ExecutionEngine[Execution Engine]
        DatabaseProviderMetadata[Metadata]
        CustomLitebridge[Custom Litebridge API]
        style CustomLitebridge fill:#e6f2ff,stroke:#00b,stroke-width:1px
    end

    DataSource[Data Source]
    style DataSource fill:#e6e6e6
    
    RegistrationApi -->|Creates DTO-table mappings| RegistrationEngine
    MergeApi --> MergeEngine --->|Generates & executes SQL| DatabaseProvider
    SelectApi --> SelectEngine -->|Generates & executes SQL| DatabaseProvider
    InsertApi --> InsertEngine -->|Generates & executes SQL| DatabaseProvider
    InsertApi --> PersistenceFacade --> InsertStatementBuilder -->|Models statement| InsertEngine
    UpdateApi --> PersistenceFacade --> UpdateStatementBuilder -->|Models statement| UpdateEngine
    UpdateApi --> PersistenceFacade --> DeleteStatementBuilder -->|Models statement| DeleteEngine
    DeleteApi --> PersistenceFacade --> DeleteEngine -->|Generates SQL & executes| DatabaseProvider
    PersistenceFacade --> ChangeTracker
    UpdateApi --> UpdateEngine -->|Generates & executes SQL| DatabaseProvider
    DeleteApi --> DeleteEngine -->|Generates & executes SQL| DatabaseProvider
    NativeSqlApi --> NativeSqlEngine -->|Executes SQL| DatabaseProvider
    TransactionsApi --> TransactionManager
    EngineGroup --> QueryCompilerGroup
    NativeSqlEngine --> NativeSqlCache
    EngineGroup --> TableRegistry
    TableRegistry --> DatabaseProviderMetadata
    LitebrideBuilder --> CustomLitebridge
    DatabaseProviderMetadata --> DataSource
    ExecutionEngine --> DataSource
```

## Basic API flow

The `Litebridge` instance is the primary entry point to accessing Litebridge's functionality. 
Its actual implementation is dependent on the `DatabaseProvider` instance used, allowing 
the database provider to limit or extend the Litebridge API to support vendor-specific features.
All custom instances of Litebridge must extend `LitebridgeCore` at a minimum.

The API itself is a lightweight, fluent API that generates a chain of `QueryNode` instances which are evaluated
against a query structure cache before SQL generation. If the query's hash isn't found in the cache, one of the 
core ORM's statement engines will compile the query node chain into a logical model of the SQL operation
(e.g. a `Select`, `Insert`, etc) using the `QueryCompiler`. The result is passed to the `DatabaseProvider` for SQL string generation. 
The generated SQL string is cached with metadata, and the statement is executed with extract bind parameters.
Subsequent executions of the same structural query skip the query compilation and SQL generation phases due to the cached data.

