/* Disable constraints for the duration of this script */
DECLARE 
   CURSOR constraint_cursor IS 
      SELECT table_name, constraint_name 
         FROM user_constraints 
         WHERE constraint_type = 'R'
           AND status = 'ENABLED';
BEGIN 
   FOR r IN constraint_cursor LOOP
      execute immediate 'ALTER TABLE '||r.table_name||' DISABLE CONSTRAINT '||r.constraint_name; 
   END LOOP; 
END;
/

/*  Clear out tables we don't need rows in  */

DECLARE
  CURSOR tables_to_empty IS
    SELECT table_name
      FROM user_tables
      WHERE table_name like 'KR%' 
      and table_name NOT IN ( 
	  'KR_COUNTRY_T'
	, 'KR_POSTAL_CODE_T'
	, 'KR_COUNTY_T'
	, 'KR_STATE_T'
	, 'KREW_DLGN_RSP_T'
	, 'KREW_DOC_TYP_ATTR_T'
	, 'KREW_DOC_TYP_PLCY_RELN_T'
	, 'KREW_DOC_TYP_PROC_T'
	, 'KREW_DOC_TYP_T'
	, 'KREW_RTE_BRCH_PROTO_T'
	, 'KREW_RTE_BRCH_ST_T'
	, 'KREW_RTE_BRCH_T'
	, 'KREW_RTE_NODE_CFG_PARM_T'
	, 'KREW_RTE_NODE_LNK_T'
	, 'KREW_RTE_NODE_T'
	, 'KREW_RULE_ATTR_T'
	, 'KREW_RULE_EXT_T'
	, 'KREW_RULE_EXT_VAL_T'
	, 'KREW_RULE_RSP_T'
	, 'KREW_RULE_T'
	, 'KREW_RULE_TMPL_ATTR_T'
	, 'KREW_RULE_TMPL_OPTN_T'
	, 'KREW_RULE_TMPL_T'
	, 'KREW_STYLE_T'
	, 'KRIM_ADDR_TYP_T'
	, 'KRIM_AFLTN_TYP_T'
	, 'KRIM_ATTR_DEFN_T'
	, 'KRIM_CTZNSHP_STAT_T'
	, 'KRIM_DLGN_MBR_ATTR_DATA_T'
	, 'KRIM_DLGN_MBR_T'
	, 'KRIM_DLGN_T'
	, 'KRIM_EMAIL_TYP_T'
	, 'KRIM_EMP_STAT_T'
	, 'KRIM_EMP_TYP_T'
	, 'KRIM_ENT_NM_TYP_T'
	, 'KRIM_ENT_TYP_T'
	, 'KRIM_EXT_ID_TYP_T'
	, 'KRIM_PERM_ATTR_DATA_T'
	, 'KRIM_PERM_T'
	, 'KRIM_PERM_TMPL_T'
	, 'KRIM_PHONE_TYP_T'
	, 'KRIM_ROLE_MBR_ATTR_DATA_T'
	, 'KRIM_ROLE_MBR_T'
	, 'KRIM_ROLE_PERM_T'
	, 'KRIM_ROLE_RSP_ACTN_T'
	, 'KRIM_ROLE_RSP_T'
	, 'KRIM_ROLE_T'
	, 'KRIM_RSP_ATTR_DATA_T'
	, 'KRIM_RSP_T'
	, 'KRIM_RSP_TMPL_T'
	, 'KRIM_TYP_ATTR_T'
	, 'KRIM_TYP_T'
	, 'KRNS_CMP_TYP_T'
	, 'KRNS_NMSPC_T'
	, 'KRNS_NTE_TYP_T'
	, 'KRNS_PARM_T'
	, 'KRNS_PARM_TYP_T'
	, 'KRNS_PARM_DTL_TYP_T'
	, 'KRSB_QRTZ_LOCKS'
	, 'KRSB_QRTZ_TRIGGERS'
	, 'KRSB_QRTZ_CRON_TRIGGERS'
	);
BEGIN
  FOR rec IN tables_to_empty LOOP
    dbms_output.put_line( 'Truncated Table: '||rec.table_name );
    EXECUTE IMMEDIATE 'TRUNCATE TABLE '||rec.table_name;
  END LOOP;
END;
/

/*  Users & Groups  */

DELETE FROM KRIM_ROLE_MBR_T WHERE MBR_TYP_CD='P'
/
DELETE FROM KRIM_DLGN_MBR_T WHERE MBR_TYP_CD='P'
/

/* these next two delete most of the info in these tables,
 * the little that is left probably needs to be reviewed by
 * implementing schools to verify that data. without the
 * entries though, routing fails on most transactional docs */

DELETE FROM KRIM_ROLE_RSP_ACTN_T WHERE ROLE_MBR_ID <> '*'
/
DELETE FROM KRIM_ROLE_MBR_ATTR_DATA_T WHERE ROLE_MBR_ID NOT IN (SELECT ROLE_MBR_ID from KRIM_ROLE_MBR_T where MBR_TYP_CD='R')
/

/* RE-INSERT to user and workgroup member table (admin) */
INSERT INTO krim_prncpl_t
(prncpl_id,"PRNCPL_NM",entity_id,obj_id)
VALUES
('1','kr','1',SYS_GUID())
/
INSERT INTO krim_prncpl_t
(prncpl_id,"PRNCPL_NM",entity_id,obj_id)
VALUES
('2','kfs','2',SYS_GUID())
/
INSERT INTO krim_prncpl_t
(prncpl_id,"PRNCPL_NM",entity_id,obj_id)
VALUES
('3','admin','3',SYS_GUID())
/
INSERT INTO krim_entity_t
(entity_id,obj_id)
VALUES
('1',SYS_GUID())
/
INSERT INTO krim_entity_t
(entity_id,obj_id)
VALUES
('2',SYS_GUID())
/
INSERT INTO krim_entity_t
(entity_id,obj_id)
VALUES
('3',SYS_GUID())
/

INSERT INTO KRIM_ENTITY_ENT_TYP_T
("ENT_TYP_CD",entity_id,obj_id)
VALUES
('SYSTEM','1',SYS_GUID())
/
INSERT INTO KRIM_ENTITY_ENT_TYP_T
("ENT_TYP_CD",entity_id,obj_id)
VALUES
('SYSTEM','2',SYS_GUID())
/
INSERT INTO KRIM_ENTITY_ENT_TYP_T
("ENT_TYP_CD",entity_id,obj_id)
VALUES
('PERSON','3',SYS_GUID())
/
INSERT INTO KRIM_ENTITY_NM_T
("ENTITY_NM_ID",obj_id,entity_id,"NM_TYP_CD","FIRST_NM","MIDDLE_NM","LAST_NM","DFLT_IND")
VALUES
('1',SYS_GUID(),'3','PRFR','KFS','','Admin', 'Y')
/
INSERT INTO KRIM_ENTITY_AFLTN_T
("ENTITY_AFLTN_ID",OBJ_ID,ENTITY_ID,"AFLTN_TYP_CD","CAMPUS_CD","DFLT_IND")
VALUES
('1',SYS_GUID(),'3','STAFF','01','Y')
/
INSERT INTO KRIM_ENTITY_EMP_INFO_T
("ENTITY_EMP_ID",OBJ_ID,ENTITY_ID,"ENTITY_AFLTN_ID","EMP_STAT_CD","EMP_TYP_CD","BASE_SLRY_AMT","PRMRY_IND")
VALUES
('1',SYS_GUID(),'3','1','A','P',100000,'Y')
/
INSERT INTO KRIM_ROLE_MBR_T
("ROLE_MBR_ID",OBJ_ID,"ROLE_ID","MBR_ID")
VALUES
('1',SYS_GUID(),'90','1')
/
INSERT INTO KRIM_ROLE_MBR_T
("ROLE_MBR_ID",OBJ_ID,"ROLE_ID","MBR_ID")
VALUES
('2',SYS_GUID(),'62','2')
/
INSERT INTO KRIM_ROLE_MBR_T
("ROLE_MBR_ID",OBJ_ID,"ROLE_ID","MBR_ID")
VALUES
('3',SYS_GUID(),'45','3')
/
INSERT INTO KRIM_ROLE_MBR_T
("ROLE_MBR_ID",OBJ_ID,"ROLE_ID","MBR_ID")
VALUES
('4',SYS_GUID(),'11','3')
/
INSERT INTO KRIM_ROLE_MBR_T
("ROLE_MBR_ID",OBJ_ID,"ROLE_ID","MBR_ID")
VALUES
('5',SYS_GUID(),'12','3')
/
INSERT INTO KRIM_ROLE_MBR_T
("ROLE_MBR_ID",OBJ_ID,"ROLE_ID","MBR_ID")
VALUES
('6',SYS_GUID(),'13','3')
/
INSERT INTO KRIM_ROLE_MBR_T
("ROLE_MBR_ID",OBJ_ID,"ROLE_ID","MBR_ID")
VALUES
('7',SYS_GUID(),'15','3')
/
INSERT INTO KRIM_ROLE_MBR_T
("ROLE_MBR_ID",OBJ_ID,"ROLE_ID","MBR_ID")
VALUES
('8',SYS_GUID(),'16','3')
/
INSERT INTO KRIM_ROLE_MBR_T
("ROLE_MBR_ID",OBJ_ID,"ROLE_ID","MBR_ID")
VALUES
('11',SYS_GUID(),'22','3')
/
INSERT INTO KRIM_ROLE_MBR_T
("ROLE_MBR_ID",OBJ_ID,"ROLE_ID","MBR_ID")
VALUES
('12',SYS_GUID(),'25','3')
/
INSERT INTO KRIM_ROLE_MBR_T
("ROLE_MBR_ID",OBJ_ID,"ROLE_ID","MBR_ID")
VALUES
('14',SYS_GUID(),'29','3')
/
INSERT INTO KRIM_ROLE_MBR_T
("ROLE_MBR_ID",OBJ_ID,"ROLE_ID","MBR_ID")
VALUES
('15',SYS_GUID(),'30','3')
/
INSERT INTO KRIM_ROLE_MBR_T
("ROLE_MBR_ID",OBJ_ID,"ROLE_ID","MBR_ID")
VALUES
('16',SYS_GUID(),'31','3')
/
INSERT INTO KRIM_ROLE_MBR_T
("ROLE_MBR_ID",OBJ_ID,"ROLE_ID","MBR_ID")
VALUES
('17',SYS_GUID(),'34','3')
/
INSERT INTO KRIM_ROLE_MBR_T
("ROLE_MBR_ID",OBJ_ID,"ROLE_ID","MBR_ID")
VALUES
('18',SYS_GUID(),'36','3')
/
INSERT INTO KRIM_ROLE_MBR_T
("ROLE_MBR_ID",OBJ_ID,"ROLE_ID","MBR_ID")
VALUES
('19',SYS_GUID(),'37','3')
/
INSERT INTO KRIM_ROLE_MBR_T
("ROLE_MBR_ID",OBJ_ID,"ROLE_ID","MBR_ID")
VALUES
('20',SYS_GUID(),'38','3')
/
INSERT INTO KRIM_ROLE_MBR_T
("ROLE_MBR_ID",OBJ_ID,"ROLE_ID","MBR_ID")
VALUES
('22',SYS_GUID(),'40','3')
/
INSERT INTO KRIM_ROLE_MBR_T
("ROLE_MBR_ID",OBJ_ID,"ROLE_ID","MBR_ID")
VALUES
('23',SYS_GUID(),'46','3')
/
INSERT INTO KRIM_ROLE_MBR_T
("ROLE_MBR_ID",OBJ_ID,"ROLE_ID","MBR_ID")
VALUES
('24',SYS_GUID(),'47','3')
/
INSERT INTO KRIM_ROLE_MBR_T
("ROLE_MBR_ID",OBJ_ID,"ROLE_ID","MBR_ID")
VALUES
('26',SYS_GUID(),'49','3')
/
INSERT INTO KRIM_ROLE_MBR_T
("ROLE_MBR_ID",OBJ_ID,"ROLE_ID","MBR_ID")
VALUES
('27',SYS_GUID(),'50','3')
/
INSERT INTO KRIM_ROLE_MBR_T
("ROLE_MBR_ID",OBJ_ID,"ROLE_ID","MBR_ID")
VALUES
('28',SYS_GUID(),'51','3')
/
INSERT INTO KRIM_ROLE_MBR_T
("ROLE_MBR_ID",OBJ_ID,"ROLE_ID","MBR_ID")
VALUES
('29',SYS_GUID(),'54','3')
/
INSERT INTO KRIM_ROLE_MBR_T
("ROLE_MBR_ID",OBJ_ID,"ROLE_ID","MBR_ID")
VALUES
('30',SYS_GUID(),'55','3')
/
INSERT INTO KRIM_ROLE_MBR_T
("ROLE_MBR_ID",OBJ_ID,"ROLE_ID","MBR_ID")
VALUES
('32',SYS_GUID(),'65','3')
/
INSERT INTO KRIM_ROLE_MBR_T
("ROLE_MBR_ID",OBJ_ID,"ROLE_ID","MBR_ID")
VALUES
('34',SYS_GUID(),'7','3')
/
INSERT INTO KRIM_ROLE_MBR_T
("ROLE_MBR_ID",OBJ_ID,"ROLE_ID","MBR_ID")
VALUES
('35',SYS_GUID(),'76','3')
/
INSERT INTO KRIM_ROLE_MBR_T
("ROLE_MBR_ID",OBJ_ID,"ROLE_ID","MBR_ID")
VALUES
('36',SYS_GUID(),'79','3')
/

INSERT INTO KRIM_ROLE_MBR_ATTR_DATA_T
("ATTR_DATA_ID",OBJ_ID,"ROLE_MBR_ID","KIM_TYP_ID","KIM_ATTR_DEFN_ID","ATTR_VAL")
VALUES
('1',SYS_GUID(),'6','26','22','01')
/
INSERT INTO KRIM_ROLE_MBR_ATTR_DATA_T
("ATTR_DATA_ID",OBJ_ID,"ROLE_MBR_ID","KIM_TYP_ID","KIM_ATTR_DEFN_ID","ATTR_VAL")
VALUES
('2',SYS_GUID(),'6','26','23','0001')
/
INSERT INTO KRIM_ROLE_MBR_ATTR_DATA_T
("ATTR_DATA_ID",OBJ_ID,"ROLE_MBR_ID","KIM_TYP_ID","KIM_ATTR_DEFN_ID","ATTR_VAL")
VALUES
('3',SYS_GUID(),'4','17','12','01')
/
INSERT INTO KRIM_ROLE_MBR_ATTR_DATA_T
("ATTR_DATA_ID",OBJ_ID,"ROLE_MBR_ID","KIM_TYP_ID","KIM_ATTR_DEFN_ID","ATTR_VAL")
VALUES
('4',SYS_GUID(),'5','17','12','01')
/
INSERT INTO KRIM_ROLE_MBR_ATTR_DATA_T
("ATTR_DATA_ID",OBJ_ID,"ROLE_MBR_ID","KIM_TYP_ID","KIM_ATTR_DEFN_ID","ATTR_VAL")
VALUES
('5',SYS_GUID(),'12','34','32','99')
/
INSERT INTO KRIM_ROLE_MBR_ATTR_DATA_T
("ATTR_DATA_ID",OBJ_ID,"ROLE_MBR_ID","KIM_TYP_ID","KIM_ATTR_DEFN_ID","ATTR_VAL")
VALUES
('6',SYS_GUID(),'15','27','22','01')
/
INSERT INTO KRIM_ROLE_MBR_ATTR_DATA_T
("ATTR_DATA_ID",OBJ_ID,"ROLE_MBR_ID","KIM_TYP_ID","KIM_ATTR_DEFN_ID","ATTR_VAL")
VALUES
('7',SYS_GUID(),'15','27','24','0001')
/
INSERT INTO KRIM_ROLE_MBR_ATTR_DATA_T
("ATTR_DATA_ID",OBJ_ID,"ROLE_MBR_ID","KIM_TYP_ID","KIM_ATTR_DEFN_ID","ATTR_VAL")
VALUES
('8',SYS_GUID(),'19','25','22','01')
/
INSERT INTO KRIM_ROLE_MBR_ATTR_DATA_T
("ATTR_DATA_ID",OBJ_ID,"ROLE_MBR_ID","KIM_TYP_ID","KIM_ATTR_DEFN_ID","ATTR_VAL")
VALUES
('9',SYS_GUID(),'29','36','4','KFS-SYS')
/
INSERT INTO KRIM_ROLE_MBR_ATTR_DATA_T
("ATTR_DATA_ID",OBJ_ID,"ROLE_MBR_ID","KIM_TYP_ID","KIM_ATTR_DEFN_ID","ATTR_VAL")
VALUES
('10',SYS_GUID(),'29','36','22','01')
/
INSERT INTO KRIM_ROLE_MBR_ATTR_DATA_T
("ATTR_DATA_ID",OBJ_ID,"ROLE_MBR_ID","KIM_TYP_ID","KIM_ATTR_DEFN_ID","ATTR_VAL")
VALUES
('11',SYS_GUID(),'29','36','24','0001')
/
INSERT INTO KRIM_ROLE_MBR_ATTR_DATA_T
("ATTR_DATA_ID",OBJ_ID,"ROLE_MBR_ID","KIM_TYP_ID","KIM_ATTR_DEFN_ID","ATTR_VAL")
VALUES
('12',SYS_GUID(),'34','29','22','01')
/
INSERT INTO KRIM_ROLE_MBR_ATTR_DATA_T
("ATTR_DATA_ID",OBJ_ID,"ROLE_MBR_ID","KIM_TYP_ID","KIM_ATTR_DEFN_ID","ATTR_VAL")
VALUES
('13',SYS_GUID(),'34','29','24','0001')
/
INSERT INTO KRIM_ROLE_MBR_ATTR_DATA_T
("ATTR_DATA_ID",OBJ_ID,"ROLE_MBR_ID","KIM_TYP_ID","KIM_ATTR_DEFN_ID","ATTR_VAL")
VALUES
('14',SYS_GUID(),'34','29','13','ACCT')
/
INSERT INTO KRIM_ROLE_MBR_ATTR_DATA_T
("ATTR_DATA_ID",OBJ_ID,"ROLE_MBR_ID","KIM_TYP_ID","KIM_ATTR_DEFN_ID","ATTR_VAL")
VALUES
('15',SYS_GUID(),'35','27','22','01')
/
INSERT INTO KRIM_ROLE_MBR_ATTR_DATA_T
("ATTR_DATA_ID",OBJ_ID,"ROLE_MBR_ID","KIM_TYP_ID","KIM_ATTR_DEFN_ID","ATTR_VAL")
VALUES
('16',SYS_GUID(),'35','27','24','0001')
/
INSERT INTO KRIM_ROLE_MBR_ATTR_DATA_T
("ATTR_DATA_ID",OBJ_ID,"ROLE_MBR_ID","KIM_TYP_ID","KIM_ATTR_DEFN_ID","ATTR_VAL")
VALUES
('17',SYS_GUID(),'36','25','22','0001')
/

COMMIT
/

-- Clean up person responsibility references
UPDATE KREW_RULE_RSP_T
    SET NM = '1'
    WHERE TYP = 'F'
/

/*  System Parameters & Rules  */
-- blank out rules which contain values from other emptied tables

--SELECT a.sh_parm_nmspc_cd, a.sh_parm_dtl_typ_cd, a.sh_parm_nm, a.sh_parm_typ_cd, a.sh_parm_txt
--  FROM sh_parm_t a
  UPDATE KRNS_PARM_T 
	SET TXT = NULL
  WHERE TXT IS NOT NULL
    AND ( 
		parm_nm LIKE '%CHARTS'
	 OR parm_nm LIKE '%CHART'
	 OR parm_nm LIKE '%CHART_CODE'
	 OR parm_nm LIKE '%OBJECT_CODE%'
	 OR parm_nm LIKE '%OBJECT_LEVEL%'
	 OR parm_nm LIKE '%OBJECT_CONS%'
	 OR parm_nm LIKE '%CAMPUS%'
	 OR parm_nm LIKE '%ORIGINATIONS%'
	 OR parm_nm LIKE '%ACCOUNT%'
	 OR parm_nm LIKE '%BANK_ACCOUNT%'
	 OR parm_nm LIKE '%ORGANIZATION'
	 OR parm_nm LIKE '%USER'
   )
   AND parm_nm NOT LIKE '%GROUP'
   AND parm_nm <> 'SUB_ACCOUNT_TYPES'
   AND parm_nm NOT LIKE '%OBJECT_SUB_TYPES'
   AND parm_nm NOT LIKE 'MAX_FILE_SIZE%'
   AND parm_nm <> 'MAXIMUM_ACCOUNT_RESPONSIBILITY_ID'
/
COMMIT
/

/* Fix the sh_campus_t table */
insert into KRNS_CAMPUS_T values ('01','Default Campus','Campus','F',sys_guid(),1,'Y')
/

/* Re-enable constraints */
DECLARE 
   CURSOR constraint_cursor IS 
      SELECT table_name, constraint_name 
         FROM user_constraints 
         WHERE constraint_type = 'R'
           AND status <> 'ENABLED';
BEGIN 
   FOR r IN constraint_cursor LOOP
      execute immediate 'ALTER TABLE '||r.table_name||' ENABLE CONSTRAINT '||r.constraint_name; 
   END LOOP; 
END;
/