DROP TABLE IF EXISTS `IFCategories`;
DROP TABLE IF EXISTS `IFActions`;
DROP TABLE IF EXISTS `IFData`;
DROP TABLE IF EXISTS `IFMimeTypes`;
DROP TABLE IF EXISTS `IntentFilters`;
DROP TABLE IF EXISTS `Aliases`;
DROP TABLE IF EXISTS `ExitPointComponents`;
DROP TABLE IF EXISTS `PAuthorities`;
DROP TABLE IF EXISTS `Providers`;
DROP TABLE IF EXISTS `Components`;
DROP TABLE IF EXISTS `ComponentExtras`;
DROP TABLE IF EXISTS `ICategories`;
DROP TABLE IF EXISTS `IActions`;
DROP TABLE IF EXISTS `IMimeTypes`;
DROP TABLE IF EXISTS `IExtras`;
DROP TABLE IF EXISTS `IPackages`;
DROP TABLE IF EXISTS `IClasses`;
DROP TABLE IF EXISTS `IData`;
DROP TABLE IF EXISTS `Intents`;
DROP TABLE IF EXISTS `IntentPermissions`;
DROP TABLE IF EXISTS `Uris`;
DROP TABLE IF EXISTS `UriData`;
DROP TABLE IF EXISTS `ExitPoints`;
DROP TABLE IF EXISTS `Classes`;
DROP TABLE IF EXISTS `CategoryStrings`;
DROP TABLE IF EXISTS `ActionStrings`;
DROP TABLE IF EXISTS `UsesPermissions`;
DROP TABLE IF EXISTS `Permissions`;
DROP TABLE IF EXISTS `PermissionStrings`;
DROP TABLE IF EXISTS `Applications`;



--
-- Common.
--

CREATE TABLE `Applications` (
  `id` int NOT NULL AUTO_INCREMENT,
  `app` varchar(512) NOT NULL,
  `version` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

CREATE TABLE `PermissionStrings` (
  `id` int NOT NULL AUTO_INCREMENT,
  `st` varchar(191) NOT NULL UNIQUE,
  PRIMARY KEY (`id`),
  INDEX `st_idx` (`st`) USING HASH
) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

--
-- Using the level column in the primary key allows us to handle the case where 
-- the same permission is defined in several applications with different levels.
--
CREATE TABLE `Permissions` (
  `id` int NOT NULL,
  `level` char NOT NULL,
  PRIMARY KEY (`id`, `level`),
  FOREIGN KEY (`id`) REFERENCES PermissionStrings(`id`)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

CREATE TABLE `UsesPermissions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `app_id` int NOT NULL,
  `uses_permission` int NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`app_id`) REFERENCES Applications(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`uses_permission`) REFERENCES PermissionStrings(`id`),
  INDEX `uses_permission_idx` (`uses_permission`) USING HASH
) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

CREATE TABLE `Classes` (
  `id` int NOT NULL AUTO_INCREMENT,
  `app_id` int NOT NULL,
  `class` varchar(191) NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`app_id`) REFERENCES Applications(`id`) ON DELETE CASCADE,
  INDEX `class_idx` (`class`) USING HASH,
  INDEX `app_id_idx` (`app_id`) USING HASH
) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

CREATE TABLE `ActionStrings` (
  `id` int NOT NULL AUTO_INCREMENT,
  `st` varchar(191) NOT NULL UNIQUE,
  PRIMARY KEY (`id`),
  INDEX `st_idx` (`st`) USING HASH
) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

CREATE TABLE `CategoryStrings` (
  `id` int NOT NULL AUTO_INCREMENT,
  `st` varchar(191) NOT NULL UNIQUE,
  PRIMARY KEY (`id`),
  INDEX `st_idx` (`st`) USING HASH
) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

CREATE TABLE `UriData` (
  `id` int NOT NULL AUTO_INCREMENT,
  `scheme` varchar(128) DEFAULT NULL,
  `ssp` varchar(128) DEFAULT NULL,
  `uri` longtext DEFAULT NULL,
  `path` varchar(128) DEFAULT NULL,
  `query` varchar(512) DEFAULT NULL,
  `authority` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`id`)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;


--
-- Entry points.
--

-- missing intent filters count: 0: unknown - null: not missing.
CREATE TABLE `Components` (
  `id` int NOT NULL AUTO_INCREMENT,
  `class_id` int NOT NULL,
  `kind` char NOT NULL,
  `exported` bool NOT NULL,
  `permission` int DEFAULT NULL,
  `missing` int,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`class_id`) REFERENCES Classes(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`permission`) REFERENCES PermissionStrings(`id`)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

CREATE TABLE `ComponentExtras` (
  `id` int NOT NULL AUTO_INCREMENT,
  `component_id` int NOT NULL,
  `extra` varchar(512) NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`component_id`) REFERENCES Components(`id`)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

CREATE TABLE `Aliases` (
  `id` int NOT NULL AUTO_INCREMENT,
  `component_id` int NOT NULL,
  `target_id` int NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`component_id`) REFERENCES Components(`id`),
  FOREIGN KEY (`target_id`) REFERENCES Components(`id`)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

CREATE TABLE `Providers` (
  `id` int NOT NULL AUTO_INCREMENT,
  `component_id` int NOT NULL,
  `grant_uri_permissions` bool NOT NULL,
  `read_permission` varchar(512) DEFAULT NULL,
  `write_permission` varchar(512) DEFAULT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`component_id`) REFERENCES Components(`id`)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

CREATE TABLE `PAuthorities` (
  `id` int NOT NULL AUTO_INCREMENT,
  `provider_id` int NOT NULL,
  `authority` varchar(512) NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`provider_id`) REFERENCES Providers(`id`)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

CREATE TABLE `IntentFilters` (
  `id` int NOT NULL AUTO_INCREMENT,
  `component_id` int NOT NULL,
  `alias` bool,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`component_id`) REFERENCES Components(`id`) ON DELETE CASCADE,
  INDEX `c_id_idx` (`component_id`) USING HASH
) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

CREATE TABLE `IFActions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `filter_id` int NOT NULL,
  `action` int NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`filter_id`) REFERENCES IntentFilters(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`action`) REFERENCES ActionStrings(`id`),
  INDEX `action_idx` (`action`) USING HASH
) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

CREATE TABLE `IFCategories` (
  `id` int NOT NULL AUTO_INCREMENT,
  `filter_id` int NOT NULL,
  `category` int NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`filter_id`) REFERENCES IntentFilters(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`category`) REFERENCES CategoryStrings(`id`),
  INDEX `category_idx` (`category`) USING HASH
) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

CREATE TABLE `IFData` (
  `id` int NOT NULL AUTO_INCREMENT,
  `filter_id` int DEFAULT NULL,
  `scheme` varchar(128) DEFAULT NULL,
  `host` varchar(128) DEFAULT NULL,
  `port` varchar(128) DEFAULT NULL,
  `path` varchar(128) DEFAULT NULL,
  `type` varchar(128) DEFAULT NULL,
  `subtype` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`filter_id`) REFERENCES IntentFilters(`id`) ON DELETE CASCADE
) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

CREATE TABLE `IFMimeTypes` (
  `id` int NOT NULL AUTO_INCREMENT,
  `filter_id` int NOT NULL,
  `type` varchar(512) NOT NULL,
  `subtype` varchar(512) NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`filter_id`) REFERENCES IntentFilters(`id`) ON DELETE CASCADE,
  INDEX `type_idx` (`type`, `subtype`)
);

--
-- Exit Points.
--

-- missing count: 0: unknown - null: not missing.
CREATE TABLE `ExitPoints` (
  `id` int NOT NULL AUTO_INCREMENT,
  `class_id` int NOT NULL,
  `method` varchar(512) NOT NULL,
  `instruction` mediumint NOT NULL,
  `exit_kind` char NOT NULL,
  `missing` int,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`class_id`) REFERENCES Classes(`id`) ON DELETE CASCADE
) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

CREATE TABLE `ExitPointComponents` (
  `id` int NOT NULL AUTO_INCREMENT,
  `exit_id` int NOT NULL,
  `component_id` int NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`exit_id`) REFERENCES ExitPoints(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`component_id`) REFERENCES Components(`id`) ON DELETE CASCADE
) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

CREATE TABLE `IntentPermissions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `exit_id` int NOT NULL,
  `i_permission` int NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`exit_id`) REFERENCES ExitPoints(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`i_permission`) REFERENCES PermissionStrings(`id`)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

CREATE TABLE `Intents` (
  `id` int NOT NULL AUTO_INCREMENT,
  `exit_id` int NOT NULL,
  `implicit` bool NOT NULL,
  `alias` bool NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`exit_id`) REFERENCES ExitPoints(`id`) ON DELETE CASCADE
) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

CREATE TABLE `IActions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `intent_id` int NOT NULL,
  `action` int NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`intent_id`) REFERENCES Intents(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`action`) REFERENCES ActionStrings(`id`)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

CREATE TABLE `ICategories` (
  `id` int NOT NULL AUTO_INCREMENT,
  `intent_id` int NOT NULL,
  `category` int NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`intent_id`) REFERENCES Intents(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`category`) REFERENCES CategoryStrings(`id`)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

CREATE TABLE `IMimeTypes` (
  `id` int NOT NULL AUTO_INCREMENT,
  `intent_id` int NOT NULL,
  `type` varchar(191) NOT NULL,
  `subtype` varchar(191) NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`intent_id`) REFERENCES Intents(`id`) ON DELETE CASCADE,
  INDEX `type_idx` (`type`),
  INDEX `subtype_idx` (`subtype`)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

CREATE TABLE `IExtras` (
  `id` int NOT NULL AUTO_INCREMENT,
  `intent_id` int NOT NULL,
  `extra` varchar(512) NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`intent_id`) REFERENCES Intents(`id`) ON DELETE CASCADE
) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

CREATE TABLE `IPackages` (
  `id` int NOT NULL AUTO_INCREMENT,
  `intent_id` int NOT NULL,
  `package` varchar(512) NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`intent_id`) REFERENCES Intents(`id`) ON DELETE CASCADE
) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

CREATE TABLE `IClasses` (
  `id` int NOT NULL AUTO_INCREMENT,
  `intent_id` int NOT NULL,
  `class` varchar(512) NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`intent_id`) REFERENCES Intents(`id`) ON DELETE CASCADE
) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

CREATE TABLE `IData` (
  `id` int NOT NULL AUTO_INCREMENT,
  `intent_id` int NOT NULL,
  `data` int NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`intent_id`) REFERENCES Intents(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`data`) REFERENCES UriData(`id`) ON DELETE CASCADE
) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

CREATE TABLE `Uris` (
  `id` int NOT NULL AUTO_INCREMENT,
  `exit_id` int NOT NULL,
  `data` int,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`exit_id`) REFERENCES ExitPoints(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`data`) REFERENCES UriData(`id`) ON DELETE CASCADE
) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;


--
-- Links
--

DROP TABLE IF EXISTS `Links`;
DROP TABLE IF EXISTS `ProviderLinks`;


CREATE TABLE `Links` (
  `id` int NOT NULL AUTO_INCREMENT,
  `intent_id` int NOT NULL,
  `component_id` int NOT NULL,
  `type` int default 0,
  `reserved` varchar(512),
  PRIMARY KEY (`id`)
 ) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

CREATE TABLE `ProviderLinks` (
  `id` int NOT NULL AUTO_INCREMENT,
  `src_component_id` int NOT NULL,
  `dest_component_id` int NOT NULL,
  `reserved` varchar(512),
  PRIMARY KEY (`id`)
 ) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

DROP TABLE IF EXISTS `Stmts`;
DROP TABLE IF EXISTS `Paths`;
DROP TABLE IF EXISTS `IccStmts`;
DROP TABLE IF EXISTS `Extras`;


CREATE TABLE `Stmts` (
  `id` int NOT NULL AUTO_INCREMENT,
  `stmt` varchar(512) NOT NULL,
  `method` varchar(512) NOT NULL,
  `class_id` int NOT NULL,
  `jimpleIndex` int NOT NULL,
  `isIcc` bool NOT NULL,
  `type` varchar(512) NOT NULL,
  `reserved` varchar(512) NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `Paths` (
  `id` int NOT NULL AUTO_INCREMENT,
  `app_id` int NOT NULL,
  `source` int NOT NULL,
  `sink` int NOT NULL,
  `paths` text,
  `type` varchar(512) NOT NULL,
  `icc` int NOT NULL,
  `reserved` varchar(512) NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `IccStmts` (
  `id` int NOT NULL AUTO_INCREMENT,
  `exit_id` int NOT NULL,
  `stmt_id` int NOT NULL,
  PRIMARY KEY (`id`)
);

-- type: get | put

CREATE TABLE `Extras` (
  `id` int NOT NULL AUTO_INCREMENT,
  `method` varchar(512) NOT NULL,
  `type` varchar(512),
  `extra` varchar(512) NOT NULL,
  `reserved` varchar(512) NOT NULL,
  PRIMARY KEY (`id`)
);