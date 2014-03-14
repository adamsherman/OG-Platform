START TRANSACTION;
  UPDATE eng_schema_version SET version_value='47' WHERE version_key='schema_patch';

  DROP TABLE eng_functioncosts;

  CREATE SEQUENCE eng_fncost_seq
    START WITH 1 INCREMENT BY 1 NO CYCLE;

  CREATE TABLE eng_functioncosts (
    id bigint NOT NULL DEFAULT nextval('eng_fncost_seq'),
    configuration varchar(255) NOT NULL,
    function_name varchar(255) NOT NULL,
    version_instant timestamp without time zone NOT NULL,
    invocation_cost decimal(31,8) NOT NULL,
    data_input_cost decimal(31,8) NOT NULL,
    data_output_cost decimal(31,8) NOT NULL,
    PRIMARY KEY (id)
  );

  CREATE INDEX ix_eng_funcost_config ON eng_functioncosts(configuration);
  CREATE INDEX ix_eng_funcost_fnname ON eng_functioncosts(function_name);
  CREATE INDEX ix_eng_funcost_version ON eng_functioncosts(version_instant);

COMMIT;
