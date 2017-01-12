package eu.bcvsolutions.idm.eav.api.domain;

/**
 * Supported attribute values data type
 * 
 * @author Radek Tomiška
 *
 */
public enum PersistentType {

	CHAR,
	TEXT, 
	TEXTAREA, 
	RICHTEXTAREA,
	// TODO: SCRIPTAREA
	INT,
	LONG, 
	DOUBLE, 
	CURRENCY, 
	BOOLEAN, 
	DATE, 
	DATETIME,
	BYTE_ARRAY;
}
