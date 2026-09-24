-- ============================================================================
-- LIVE.ZDECA + LIVE.ZDECA_MATRICULA (Sage X3 / SQL Server)
-- ============================================================================
IF NOT EXISTS (SELECT 1 FROM sys.tables t
               JOIN sys.schemas s ON s.schema_id = t.schema_id
               WHERE s.name = 'LIVE' AND t.name = 'ZDECA')
BEGIN
    CREATE TABLE LIVE.ZDECA (
        SDHNUM_0   VARCHAR(20)   NOT NULL,
        ZMATTRA_0  NVARCHAR(15)  NULL,
        ZMATREM_0  NVARCHAR(15)  NULL,
        ZPESO_0    FLOAT         NULL,
        ZPALETS_0  INT           NULL,
        ZDECAURL_0 NVARCHAR(500) NULL,
        ZDECAEST_0 SMALLINT      NOT NULL DEFAULT 0,
        CREDAT_0   DATETIME      NULL,
        UPDDAT_0   DATETIME      NULL,
        CONSTRAINT PK_ZDECA PRIMARY KEY (SDHNUM_0)
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes
               WHERE name = 'IDX_ZDECA_ESTADO' AND object_id = OBJECT_ID('LIVE.ZDECA'))
    CREATE INDEX IDX_ZDECA_ESTADO ON LIVE.ZDECA (ZDECAEST_0);
GO

-- Migracion DATE -> DATETIME
IF EXISTS (SELECT 1 FROM sys.columns c JOIN sys.types t ON t.user_type_id=c.user_type_id
           WHERE c.object_id=OBJECT_ID('LIVE.ZDECA') AND c.name='CREDAT_0' AND t.name='date')
    ALTER TABLE LIVE.ZDECA ALTER COLUMN CREDAT_0 DATETIME NULL;
GO
IF EXISTS (SELECT 1 FROM sys.columns c JOIN sys.types t ON t.user_type_id=c.user_type_id
           WHERE c.object_id=OBJECT_ID('LIVE.ZDECA') AND c.name='UPDDAT_0' AND t.name='date')
    ALTER TABLE LIVE.ZDECA ALTER COLUMN UPDDAT_0 DATETIME NULL;
GO

-- Peso y palets editables (si no existen)
IF COL_LENGTH('LIVE.ZDECA', 'ZPESO_0') IS NULL
    ALTER TABLE LIVE.ZDECA ADD ZPESO_0 FLOAT NULL;
GO
IF COL_LENGTH('LIVE.ZDECA', 'ZPALETS_0') IS NULL
    ALTER TABLE LIVE.ZDECA ADD ZPALETS_0 INT NULL;
GO

-- Tabla de matriculas por transportista (carga automatica)
IF NOT EXISTS (SELECT 1 FROM sys.tables t
               JOIN sys.schemas s ON s.schema_id = t.schema_id
               WHERE s.name = 'LIVE' AND t.name = 'ZDECA_MATRICULA')
BEGIN
    CREATE TABLE LIVE.ZDECA_MATRICULA (
        BPTNUM_0   VARCHAR(20)   NOT NULL,   -- Codigo transportista (SDELIVERY.BPTNUM_0)
        ZMATTRA_0  NVARCHAR(15)  NULL,       -- Matricula tractor por defecto
        ZMATREM_0  NVARCHAR(15)  NULL,       -- Matricula remolque por defecto
        CREDAT_0   DATETIME      NULL,
        UPDDAT_0   DATETIME      NULL,
        CONSTRAINT PK_ZDECA_MATRICULA PRIMARY KEY (BPTNUM_0)
    );
END
GO
