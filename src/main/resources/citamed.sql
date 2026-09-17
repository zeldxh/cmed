/*
  Script de creacion de base de datos para CitaMed.
  Este script crea el esquema, el usuario de aplicacion,
  las tablas principales y los datos iniciales del sistema.
*/

-- Seccion de administracion
drop database if exists citamed_db;
drop user if exists usuario_citamed;
drop user if exists usuario_reportes_citamed;

-- Creacion del esquema
create database citamed_db
  default character set utf8mb4
  default collate utf8mb4_unicode_ci;

-- Creacion de usuarios
create user 'usuario_citamed'@'%' identified by 'la_Clave';
create user 'usuario_reportes_citamed'@'%' identified by 'la_Clave_Reportes';

-- Asignacion de permisos
grant select, insert, update, delete on citamed_db.* to 'usuario_citamed'@'%';
grant select on citamed_db.* to 'usuario_reportes_citamed'@'%';
flush privileges;

use citamed_db;

-- --- Seccion de Creacion de Tablas ---

-- Tabla de usuarios
create table usuario (
  id_usuario int not null auto_increment,
  nombre varchar(80) not null,
  correo varchar(100) not null unique,
  contrasena varchar(100) not null,
  rol enum('PACIENTE','DOCTOR','ADMIN') not null,
  activo boolean,
  fecha_creacion timestamp default current_timestamp,
  fecha_modificacion timestamp default current_timestamp on update current_timestamp,
  primary key (id_usuario),
  check (correo regexp '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$'),
  index ndx_usuario_correo (correo))
  engine = InnoDB;

-- Tabla de especialidades medicas
create table especialidad (
  id_especialidad int not null auto_increment,
  nombre varchar(60) not null unique,
  descripcion varchar(250),
  activo boolean,
  fecha_creacion timestamp default current_timestamp,
  fecha_modificacion timestamp default current_timestamp on update current_timestamp,
  primary key (id_especialidad),
  index ndx_especialidad_nombre (nombre))
  engine = InnoDB;

-- Tabla de pacientes
create table paciente (
  id_paciente int not null auto_increment,
  id_usuario int not null unique,
  cedula varchar(20) not null unique,
  telefono varchar(25),
  fecha_nacimiento date,
  direccion varchar(200),
  fecha_creacion timestamp default current_timestamp,
  fecha_modificacion timestamp default current_timestamp on update current_timestamp,
  primary key (id_paciente),
  index ndx_paciente_cedula (cedula),
  index ndx_paciente_usuario (id_usuario),
  foreign key fk_paciente_usuario (id_usuario) references usuario(id_usuario))
  engine = InnoDB;

-- Tabla de doctores
create table doctor (
  id_doctor int not null auto_increment,
  id_usuario int not null unique,
  id_especialidad int not null,
  numero_colegiado varchar(40) not null unique,
  activo boolean,
  fecha_creacion timestamp default current_timestamp,
  fecha_modificacion timestamp default current_timestamp on update current_timestamp,
  primary key (id_doctor),
  index ndx_doctor_usuario (id_usuario),
  index ndx_doctor_especialidad (id_especialidad),
  foreign key fk_doctor_usuario (id_usuario) references usuario(id_usuario),
  foreign key fk_doctor_especialidad (id_especialidad) references especialidad(id_especialidad))
  engine = InnoDB;

-- Tabla de horarios semanales por doctor
create table horario (
  id_horario int not null auto_increment,
  id_doctor int not null,
  dia_semana enum('LUNES','MARTES','MIERCOLES','JUEVES','VIERNES','SABADO','DOMINGO') not null,
  hora_inicio time,
  hora_fin time,
  fecha_creacion timestamp default current_timestamp,
  fecha_modificacion timestamp default current_timestamp on update current_timestamp,
  primary key (id_horario),
  check (hora_fin > hora_inicio),
  index ndx_horario_doctor (id_doctor),
  foreign key fk_horario_doctor (id_doctor) references doctor(id_doctor))
  engine = InnoDB;

-- Tabla de horarios bloqueados
create table horario_bloqueado (
  id_horario_bloqueado int not null auto_increment,
  id_doctor int not null,
  fecha_inicio datetime,
  fecha_fin datetime,
  motivo varchar(200),
  fecha_creacion timestamp default current_timestamp,
  fecha_modificacion timestamp default current_timestamp on update current_timestamp,
  primary key (id_horario_bloqueado),
  check (fecha_fin > fecha_inicio),
  index ndx_bloqueado_doctor (id_doctor),
  foreign key fk_bloqueado_doctor (id_doctor) references doctor(id_doctor))
  engine = InnoDB;

-- Tabla de citas medicas
create table cita (
  id_cita int not null auto_increment,
  id_paciente int not null,
  id_doctor int not null,
  fecha date not null,
  hora time not null,
  motivo varchar(250) not null,
  estado enum('PENDIENTE','CONFIRMADA','COMPLETADA','CANCELADA') not null,
  notas text,
  fecha_creacion timestamp default current_timestamp,
  fecha_modificacion timestamp default current_timestamp on update current_timestamp,
  primary key (id_cita),
  index ndx_cita_paciente (id_paciente),
  index ndx_cita_doctor (id_doctor),
  index ndx_cita_fecha (fecha),
  index ndx_cita_estado (estado),
  foreign key fk_cita_paciente (id_paciente) references paciente(id_paciente),
  foreign key fk_cita_doctor (id_doctor) references doctor(id_doctor))
  engine = InnoDB;

-- Tabla de notificaciones enviadas por correo (investigacion adicional: Spring Mail)
create table notificacion (
  id_notificacion int not null auto_increment,
  id_cita int not null,
  tipo enum('CONFIRMADA','RECHAZADA','CANCELADA','REPROGRAMADA') not null,
  destinatario varchar(100) not null,
  exito boolean not null,
  fecha_envio datetime not null,
  primary key (id_notificacion),
  index ndx_notificacion_cita (id_cita),
  foreign key fk_notificacion_cita (id_cita) references cita(id_cita))
  engine = InnoDB;

-- --- Seccion de Insercion de Datos ---

-- Insercion de usuarios
-- La contrasena de los cuatro usuarios de prueba es "123", protegida con BCrypt.
insert into usuario (nombre, correo, contrasena, rol, activo) values
('Jeferson Andrew Fuentes García', 'andrew@citamed.com', '$2a$10$XyIj4Q/A880L7gRzQkTrPeQH5iSBnXEhYz2fPrmj66lk6UbbfhuEO', 'ADMIN', true),
('Hillary Sofía Aguilar Chavarría', 'hillary@citamed.com', '$2a$10$ns9ZzqUNDxgYOO/SHlN5eulVq5TbWfI8/voeZdS9sx3y5RUb7Zwfy', 'PACIENTE', true),
('Dilan Steef Calvo Monge', 'dilan@citamed.com', '$2a$10$q3KOEvoZc.FEJmTgm.KbuuvCRRa1evDmerkpett6qZ.aslOrevSgq', 'DOCTOR', true),
('Fernando Esteban Ramírez Pérez', 'fernando@citamed.com', '$2a$10$alzPVkvhI5IhnqJ95oaHVuYUG3L7XoXaF12gKFy4GAq4cy07zkySG', 'DOCTOR', true);

-- Insercion de especialidades
insert into especialidad (nombre, descripcion, activo) values
('Medicina General', 'Atencion primaria y seguimiento general.', true),
('Cardiologia', 'Evaluacion y control del sistema cardiovascular.', true),
('Pediatria', 'Atencion medica para ninos y adolescentes.', true),
('Dermatologia', 'Diagnostico y tratamiento de condiciones de piel.', true),
('Ginecologia', 'Salud integral femenina.', true),
('Ortopedia', 'Lesiones y condiciones del sistema musculoesqueletico.', true);

-- Insercion de pacientes
insert into paciente (id_usuario, cedula, telefono, fecha_nacimiento, direccion) values
(2, '1-1111-1111', '8888-1111', '2000-03-15', 'San José, Costa Rica');

-- Insercion de doctores
insert into doctor (id_usuario, id_especialidad, numero_colegiado, activo) values
(3, 1, 'MED-1001', true),
(4, 2, 'MED-1002', true);

-- Insercion de horarios
insert into horario (id_doctor, dia_semana, hora_inicio, hora_fin) values
(1, 'LUNES', '08:00:00', '12:00:00'),
(1, 'MIERCOLES', '13:00:00', '17:00:00'),
(2, 'MARTES', '08:00:00', '12:00:00'),
(2, 'JUEVES', '13:00:00', '17:00:00');

-- Insercion de citas
insert into cita (id_paciente, id_doctor, fecha, hora, motivo, estado, notas) values
(1, 1, current_date(), '09:00:00', 'Control general', 'PENDIENTE', null),
(1, 2, date_add(current_date(), interval 1 day), '10:30:00', 'Revision cardiologica', 'CONFIRMADA', null),
(1, 1, date_add(current_date(), interval 2 day), '11:00:00', 'Consulta de seguimiento', 'PENDIENTE', null),
(1, 1, date_sub(current_date(), interval 7 day), '08:30:00', 'Seguimiento', 'COMPLETADA', 'Paciente estable.');
