package com.ivieleague.dejanko.orm

import com.github.jasync.sql.db.SuspendingConnection

object Settings {
    lateinit var defaultDb: SuspendingConnection
}