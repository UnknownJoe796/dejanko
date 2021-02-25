package com.ivieleague.dejanko

import com.github.jasync.sql.db.SuspendingConnection

object Settings {
    lateinit var defaultDb: SuspendingConnection
}