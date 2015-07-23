package org.moscatocms.liquibase

case class DbConfig(
  url: String,
  driver: String,
  username: Option[String],
  password: Option[String]
)