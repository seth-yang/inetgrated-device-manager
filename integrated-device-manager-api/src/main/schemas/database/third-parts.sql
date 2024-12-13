drop table if exists device;
create table device (
    imei                        varchar(64)                     not null primary key,
    module_name                 varchar(64),                                            -- 设备所属的模块名称
    protocol                    varchar(64),                                            -- 通信协议
    vendor                      varchar(64),                                            -- 三元组之厂商
    device_category             varchar(64),                                            -- 三元组之类型
    device_model                varchar(64),                                            -- 三元组之型号
    register_time               timestamp                                               -- 注册时间
);

drop table if exists device_command;
create table device_command (
    id                          serial                          not null primary key,
    imei                        varchar(64),
    command                     text,                                                   -- 命令内容，json格式
    receive_time                timestamp,                                              -- 接收时间
    performed                   boolean,                                                -- 是否已经处理
    downlink_time               timestamp,                                              -- 下发时间
    downlink_state              varchar(16),                                            -- 下发状态
    downlink_detail             text                                                    -- 下发详情
);
create index idx_dc_imei on device_command (imei);

drop table if exists raw_data_log;
create table raw_data_log (
    id                          varchar(32),
    module_name                 varchar(64),                                            -- 设备所属的模块名称
    vendor                      varchar(64),                                            -- 三元组之厂商
    device_category             varchar(64),                                            -- 三元组之类型
    device_model                varchar(64),                                            -- 三元组之型号
    remote_address              varchar(1600),                                          -- 远程对端地址
    raw_data                    text,                                                   -- 原始数据
    receive_time                timestamp                                               -- 接收时间
) partition by range (receive_time);
create index idx_rdl_time on raw_data_log (receive_time);

drop table downlink_serial;
create table downlink_serial (
    id                          varchar(32)         not null primary key,
    imei                        varchar(64),
    module_name                 varchar(64),
    current_value               int                 default 1
);
create index idx_ds_module on downlink_serial (module_name);
create index idx_ds_imei on downlink_serial (imei);

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

-- 三元组和模块/协议配置表
drop table if exists device_type_config;
create table device_type_config (
    id                          varchar(32)     not null primary key,
    module_name                 varchar(64),                                            -- 设备所属的模块名称
    device_protocol             varchar(16),                                            -- 设备通信协议
    vendor                      varchar(64),                                            -- 三元组之厂商
    device_category             varchar(64),                                            -- 三元组之类型
    device_model                varchar(64)                                             -- 三元组之型号
);

drop table if exists mqtt_log;
create table mqtt_log (
    id                          varchar(32),
    topic                       varchar(128),
    payload                     text,
    send_time                   timestamp
) partition by range (send_time);
create index idx_mqtt_time on mqtt_log (send_time);

CREATE OR REPLACE FUNCTION f_create_partition (p_table_name VARCHAR, v_date TIMESTAMP) RETURNS VOID AS $$
DECLARE
    v_end       TIMESTAMP;
    v_year      INTEGER;
    v_month     INTEGER;
    v_part_name VARCHAR;
    v_count     INTEGER := 0;
    v_cmd       VARCHAR;
BEGIN
    v_date  := date_trunc ('month', v_date);
    v_year  := extract('year' from v_date);
    v_month := extract('month' from v_date);

    IF v_month < 10 THEN
        v_part_name := p_table_name || '_' || v_year || '0' || v_month;
    ELSE
        v_part_name := p_table_name || '_' || v_year || v_month;
    END IF;
    RAISE NOTICE 'partition name = %', v_part_name;

    SELECT COUNT(*) INTO v_count FROM pg_tables WHERE tablename = v_part_name;
    IF v_count > 0 THEN
        RAISE NOTICE 'the partition table [%] exists. ignore it', v_part_name;
    ELSE
        v_end   := v_date + interval '1 month';
        v_cmd = 'CREATE TABLE ' || v_part_name || ' PARTITION OF ' ||
                p_table_name || ' FOR VALUES FROM (''' || v_date || ''') TO (''' || v_end || ''')';

        RAISE NOTICE 'command = %', v_cmd;
        EXECUTE v_cmd;

        v_cmd := 'ALTER TABLE ' || v_part_name || ' ADD PRIMARY KEY (id)';
        EXECUTE v_cmd;
    END IF;
END;
$$ LANGUAGE 'plpgsql';

create table raw_data_log_default partition of raw_data_log default;
alter table raw_data_log_default add primary key (id);
select f_create_partition ('raw_data_log', current_timestamp::timestamp);

create table downlink_log_default partition of downlink_log default;
alter table downlink_log_default add primary key (id);
select f_create_partition ('downlink_log', current_timestamp::timestamp);

create table mqtt_log_default partition of mqtt_log default;
alter table mqtt_log_default add primary key (id);
select f_create_partition ('mqtt_log', current_timestamp::timestamp);