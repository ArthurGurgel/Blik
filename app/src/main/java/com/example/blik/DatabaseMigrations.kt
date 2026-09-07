package com.example.blik

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_13_14 =
    object : Migration(13, 14) {

        override fun migrate(
            db: SupportSQLiteDatabase
        ) {

            db.execSQL(
                """
                ALTER TABLE contas
                ADD COLUMN syncId TEXT
                """.trimIndent()
            )

            db.execSQL(
                """
                ALTER TABLE categorias
                ADD COLUMN syncId TEXT
                """.trimIndent()
            )

            db.execSQL(
                """
                ALTER TABLE cartoes
                ADD COLUMN syncId TEXT
                """.trimIndent()
            )

            db.execSQL(
                """
                ALTER TABLE movimentacoes
                ADD COLUMN syncId TEXT
                """.trimIndent()
            )

            db.execSQL(
                """
                ALTER TABLE parcelas_cartao
                ADD COLUMN syncId TEXT
                """.trimIndent()
            )

            db.execSQL(
                """
                ALTER TABLE pagamentos_fatura
                ADD COLUMN syncId TEXT
                """.trimIndent()
            )


            db.execSQL(
                """
                CREATE UNIQUE INDEX IF NOT EXISTS
                index_contas_syncId
                ON contas(syncId)
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE UNIQUE INDEX IF NOT EXISTS
                index_categorias_syncId
                ON categorias(syncId)
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE UNIQUE INDEX IF NOT EXISTS
                index_cartoes_syncId
                ON cartoes(syncId)
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE UNIQUE INDEX IF NOT EXISTS
                index_movimentacoes_syncId
                ON movimentacoes(syncId)
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE UNIQUE INDEX IF NOT EXISTS
                index_parcelas_cartao_syncId
                ON parcelas_cartao(syncId)
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE UNIQUE INDEX IF NOT EXISTS
                index_pagamentos_fatura_syncId
                ON pagamentos_fatura(syncId)
                """.trimIndent()
            )
        }
    }
val MIGRATION_14_15 =
    object : Migration(14, 15) {

        override fun migrate(
            db: SupportSQLiteDatabase
        ) {

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS orcamentos (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    referencia TEXT NOT NULL,
                    mes INTEGER NOT NULL,
                    ano INTEGER NOT NULL,
                    tipo TEXT NOT NULL,
                    categoriaId INTEGER,
                    limiteMensal REAL NOT NULL,
                    ativo INTEGER NOT NULL,
                    syncId TEXT,
                    FOREIGN KEY(categoriaId)
                        REFERENCES categorias(id)
                        ON UPDATE NO ACTION
                        ON DELETE CASCADE
                )
                """.trimIndent()
            )


            db.execSQL(
                """
                CREATE UNIQUE INDEX IF NOT EXISTS
                index_orcamentos_referencia
                ON orcamentos(referencia)
                """.trimIndent()
            )


            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS
                index_orcamentos_categoriaId
                ON orcamentos(categoriaId)
                """.trimIndent()
            )


            db.execSQL(
                """
                CREATE UNIQUE INDEX IF NOT EXISTS
                index_orcamentos_syncId
                ON orcamentos(syncId)
                """.trimIndent()
            )
        }
    }