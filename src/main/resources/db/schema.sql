-- Create Tables payments Schema Start***

CREATE TABLE IF NOT EXISTS `Payment_Method` (
                                                `id`           INT          NOT NULL,
                                                `name`         VARCHAR(50)  NOT NULL,
                                                `status`       SMALLINT     DEFAULT 1,
                                                `creationDate` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `Payment_Type` (
                                              `id`           INT          NOT NULL,
                                              `type`         VARCHAR(50)  NOT NULL,
                                              `status`       SMALLINT     DEFAULT 1,
                                              `creationDate` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                              PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `Provider` (
                                          `id`           INT          NOT NULL AUTO_INCREMENT,
                                          `providerName` VARCHAR(50)  NOT NULL,
                                          `status`       SMALLINT     DEFAULT 1,
                                          `creationDate` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                          PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `Transaction` (
                                             `id`                BIGINT        NOT NULL AUTO_INCREMENT,
                                             `userId`            INT           NOT NULL,
                                             `paymentMethodId`   INT           NOT NULL,
                                             `providerId`        INT           NOT NULL,
                                             `paymentTypeId`     INT           NOT NULL,
                                             `status`            VARCHAR(10)   NOT NULL,
                                             `amount`            INT           DEFAULT 0,
                                             `currency`          VARCHAR(3)    NOT NULL,
                                             `txnReference`      VARCHAR(50)   NOT NULL,
                                             `providerReference` VARCHAR(100)  DEFAULT NULL,
                                             `clientSecret`      VARCHAR(50)   NOT NULL,
                                             `errorCode`         VARCHAR(500)  DEFAULT NULL,
                                             `errorMessage`      VARCHAR(1000) DEFAULT NULL,
                                             `creationDate`      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                             `retryCount`        INT           DEFAULT 0,
                                             PRIMARY KEY (`id`),
                                             UNIQUE (`txnReference`),
                                             CONSTRAINT `transaction_paymentMethodId` FOREIGN KEY (`paymentMethodId`) REFERENCES `Payment_Method` (`id`),
                                             CONSTRAINT `transaction_providerId`      FOREIGN KEY (`providerId`)      REFERENCES `Provider` (`id`),
                                             CONSTRAINT `transaction_paymentTypeId`   FOREIGN KEY (`paymentTypeId`)   REFERENCES `Payment_Type` (`id`)
);

CREATE INDEX `idx_transaction_paymentMethodId` ON `Transaction` (`paymentMethodId`);
CREATE INDEX `idx_transaction_providerId`      ON `Transaction` (`providerId`);
CREATE INDEX `idx_transaction_paymentTypeId`   ON `Transaction` (`paymentTypeId`);

CREATE TABLE IF NOT EXISTS `Transaction_Log` (
                                                 `id`            INT         NOT NULL AUTO_INCREMENT,
                                                 `transactionId` BIGINT      NOT NULL,
                                                 `txnFromStatus` VARCHAR(50) DEFAULT '-1',
                                                 `txnToStatus`   VARCHAR(50) DEFAULT '-1',
                                                 `creationDate`  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                 PRIMARY KEY (`id`),
                                                 CONSTRAINT `transaction_log_transactionId` FOREIGN KEY (`transactionId`) REFERENCES `Transaction` (`id`)
);

CREATE INDEX `idx_transaction_log_transactionId` ON `Transaction_Log` (`transactionId`);

-- Create Tables payments Schema End***