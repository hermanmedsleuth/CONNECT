-- -----------------------------------------------------------------------------------------------------
-- ALTER AUIDTREPO.AUDITREPOSITORY
-- IF table exists the following commands can be used to alter the table
-- -----------------------------------------------------------------------------------------------------
ALTER TABLE auditrepo.auditrepository
ADD COLUMN eventType VARCHAR(100) NOT NULL AFTER userId,
ADD COLUMN outcome TINYINT(2) NOT NULL AFTER eventType,
ADD COLUMN messageId VARCHAR(100) DEFAULT NULL AFTER outcome,
ADD COLUMN relatesTo VARCHAR(100) DEFAULT NULL AFTER messageId,
ADD COLUMN transactionId VARCHAR(100) DEFAULT NULL AFTER relatesTo,
ADD COLUMN direction CHAR(20) NOT NULL AFTER transactionId;

Alter TABLE auditrepo.auditrepository
DROP COLUMN participationTypeCode,
DROP COLUMN participationTypeCodeRole,
DROP COLUMN participationIDTypeCode,
DROP COLUMN receiverPatientId,
DROP COLUMN senderPatientId,
DROP COLUMN messageType;

ALTER TABLE auditrepo.auditrepository
CHANGE COLUMN communityId remoteHcid VARCHAR(255) DEFAULT NULL,
CHANGE COLUMN audit_timestamp eventTimestamp DATETIME NOT NULL;

ALTER TABLE auditrepo.auditrepository MODIFY COLUMN eventId VARCHAR(100) NOT NULL;

ALTER TABLE auditrepo.auditrepository MODIFY COLUMN message longblob NOT NULL;

ALTER TABLE auditrepo.auditrepository DROP INDEX UQ_eventlog_id;

ALTER TABLE auditrepo.auditrepository DROP PRIMARY KEY, MODIFY COLUMN id SERIAL PRIMARY KEY NOT NULL;