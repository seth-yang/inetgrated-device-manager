drop table if exists downlink_serial;
create table downlink_serial (
    imei                        varchar(32)         not null primary key,
    current_value               int                 default 1
);

drop table if exists device_type_config;
create table device_type_config (
    id                          varchar(32)     not null primary key,
    module_name                 varchar(64),                                            -- 设备所属的模块名称
    device_protocol             varchar(16),                                            -- 设备通信协议
    vendor                      varchar(64),                                            -- 三元组之厂商
    device_category             varchar(64),                                            -- 三元组之类型
    device_model                varchar(64)                                             -- 三元组之型号
);
create unique index idx_device_type on device_type_config (vendor, device_category, device_model);

drop table if exists downlink_log;
create table downlink_log (
    id                          varchar(32),
    imei                        varchar(32),
    module_name                 varchar(64),
    serial_no                   int,                                                    -- 下发的序号
    downlink_time               timestamp,                                              -- 下发时间戳
    downlink_status             varchar(16),                                            -- 下发状态 executing,failed,success
    reply_time                  timestamp,
    command                     text,                                                   -- 下发的实际内容
    reply                       text                                                    -- 应答内容
) partition by range (downlink_time);
create index idx_dl_time on downlink_log (downlink_time);

create table downlink_log_default partition of downlink_log default;
alter table downlink_log_default add primary key (id);
select f_create_partition ('downlink_log', current_timestamp::timestamp);

drop table if exists mqtt_log;
create table mqtt_log (
    id                          varchar(32),
    topic                       varchar(128),
    payload                     text,
    send_time                   timestamp
) partition by range (send_time);
create index idx_mqtt_time on mqtt_log (send_time);

create table mqtt_log_default partition of mqtt_log default;
alter table mqtt_log_default add primary key (id);
select f_create_partition ('mqtt_log', current_timestamp::timestamp);