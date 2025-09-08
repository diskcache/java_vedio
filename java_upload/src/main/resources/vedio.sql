create table vedio(
    id int auto_increment primary key,
    user_id int,
    title varchar(255),
    duration int unsigned,
    upload_time bigint,
    file_name varchar(50),
    hash varchar(100),
    file_path varchar(50),
    img_path varchar(50),
    foreign key (user_id) references user(id)
);
