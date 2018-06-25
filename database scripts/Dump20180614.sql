CREATE TABLE `file` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `fileId` char(36) NOT NULL unique COMMENT 'guid',
  `name` varchar(400) DEFAULT NULL COMMENT '文件名称',
  `filePath` varchar(400) DEFAULT NULL COMMENT '文件路径',
  `fileExtension` varchar(400) DEFAULT NULL COMMENT '文件扩展名',
  `fileSize` bigint(8) DEFAULT NULL COMMENT '文件大小(KB)',
  
  `status` int(11) DEFAULT NULL COMMENT '0：删除，1：未删除',
  `createtime` datetime DEFAULT NULL,
  `createuser` int(11) DEFAULT NULL,
  `updatetime` datetime DEFAULT NULL,
  `updateuser` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8 COMMENT='资源项';