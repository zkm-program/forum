ALTER TABLE user
    MODIFY tags VARCHAR(1024) DEFAULT NULL;
ALTER TABLE user
    MODIFY userAvatar VARCHAR(1024) DEFAULT NULL;
ALTER TABLE user
    ADD COLUMN isReported BOOLEAN DEFAULT false;
ALTER TABLE user
    ADD INDEX idx_userQqEmail (userQqEmail);
ALTER TABLE user
    ADD CONSTRAINT uniIdx_userQqEmail UNIQUE (userQqEmail);

ALTER TABLE tag
    ADD COLUMN parentTagName varchar(256) DEFAULT null;
ALTER TABLE tag
    DROP COLUMN parentId;
ALTER TABLE tag
    ADD CONSTRAINT uniIdx_parentTagName UNIQUE (parentTagName);