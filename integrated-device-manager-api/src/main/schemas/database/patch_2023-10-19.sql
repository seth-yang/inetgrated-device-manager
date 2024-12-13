create table tmp_downlink_serial as select * from downlink_serial;
drop table downlink_serial;
create table downlink_serial (
    id                          varchar(32)         not null primary key,
    imei                        varchar(64),
    module_name                 varchar(64),
    current_value               int                 default 1
);
create index idx_ds_module on downlink_serial (module_name);
create index idx_ds_imei on downlink_serial (imei);

create temporary sequence tmp_seq_index;
insert into downlink_serial (id, imei, module_name, current_value)
select md5 (nextval ('tmp_seq_index')::varchar) as id, imei, 'guanghelin', current_value
  from tmp_downlink_serial;