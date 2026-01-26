DO $$
DECLARE
    seq RECORD;
    tablename TEXT;
    columnname TEXT;
    max_id BIGINT;
    next_val BIGINT;
BEGIN
    FOR seq IN
        SELECT
            c.oid::regclass::text AS seqname,
            n.nspname AS schemaname,
            a.attrelid::regclass::text AS tablename,
            a.attname AS columnname
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        JOIN pg_depend d ON d.objid = c.oid
        JOIN pg_attrdef ad ON ad.oid = d.refobjid
        JOIN pg_attribute a ON a.attrelid = ad.adrelid AND a.attnum = ad.adnum
        WHERE c.relkind = 'S'
          AND d.deptype = 'a'
          AND n.nspname NOT IN ('pg_catalog', 'information_schema')
    LOOP
        EXECUTE format('SELECT MAX(%I) FROM %I', seq.columnname, seq.tablename)
        INTO max_id;

        next_val := GREATEST(COALESCE(max_id, 0) + 1, 1);

        RAISE NOTICE 'Sync sequence % with table %: next value = %', seq.seqname, seq.tablename, next_val;

        EXECUTE format('SELECT setval(''%s'', %s, false)', seq.seqname, next_val);
    END LOOP;
END $$;