package com.example.blik

import android.os.Bundle
import android.widget.Toast
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.core.view.WindowCompat
import androidx.room.Room
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.blik.ui.theme.BlikTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.handleDeeplinks

data class Movimentacao(
    val id: Int = 0,
    val descricao: String,
    val valor: Double,
    val tipo: String,
    val formaPagamento: String? = null,
    val contaId: Int? = null,
    val contaNome: String? = null,
    val contaDestinoId: Int? = null,
    val contaDestinoNome: String? = null,
    val categoriaId: Int? = null,
    val categoriaNome: String? = null,
    val cartaoId: Int? = null,
    val cartaoNome: String? = null,
    val quantidadeParcelas: Int = 1,
    val data: String
)

data class ResumoFatura(
    val cartaoId: Int,
    val cartaoNome: String,
    val mesFatura: Int,
    val anoFatura: Int,
    val restante: Double,
    val fechamento: java.util.Calendar,
    val vencimento: java.util.Calendar,
    val status: String,
    val limite: Double,
    val limiteUtilizado: Double
)


fun calcularLimiteUtilizadoCartao(
    cartaoId: Int,
    parcelasCartao: List<ParcelaCartaoComDetalhes>,
    pagamentosFatura: List<PagamentoFaturaEntity>
): Double {

    // Soma tudo que ainda compromete o limite:
    // faturas atuais + parcelas futuras.
    val totalParcelasComprometidas =
        parcelasCartao
            .filter { parcela ->
                parcela.cartaoId == cartaoId &&
                        !parcela.quitadaAnteriormente
            }
            .sumOf { parcela ->
                parcela.valor
            }


    // Cada pagamento de fatura libera aquela parte do limite.
    val totalPago =
        pagamentosFatura
            .filter { pagamento ->
                pagamento.cartaoId == cartaoId
            }
            .sumOf { pagamento ->
                pagamento.valorPago
            }


    return (
            totalParcelasComprometidas -
                    totalPago
            )
        .coerceAtLeast(0.0)
}


class MainActivity :
    ComponentActivity() {


    private var modoRecuperacaoSenha
            by mutableStateOf(false)


    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )


        tratarDeepLink(
            intent
        )

        val preferenciasAppRepository =
            PreferenciasAppRepository(
                applicationContext
            )

        setContent {

            val modoTema by
            preferenciasAppRepository
                .modoTema
                .collectAsState(
                    initial =
                        ModoTema.SISTEMA
                )

            val sistemaEscuro =
                isSystemInDarkTheme()


            val usarTemaEscuro =
                when (modoTema) {

                    ModoTema.SISTEMA ->
                        sistemaEscuro

                    ModoTema.CLARO ->
                        false

                    ModoTema.ESCURO ->
                        true
                }


            val corBarrasSistema =
                if (usarTemaEscuro) {

                    // mesmo fundo escuro principal do Blik
                    Color(0xFF0E1211)

                } else {

                    // mesmo fundo claro principal do Blik
                    Color(0xFFF7F8F8)
                }


            SideEffect {

                val controladorBarras =
                    WindowCompat.getInsetsController(
                        window,
                        window.decorView
                    )


                // Ícones ESCUROS no tema claro
                // Ícones CLAROS no tema escuro
                controladorBarras
                    .isAppearanceLightStatusBars =
                    !usarTemaEscuro


                controladorBarras
                    .isAppearanceLightNavigationBars =
                    !usarTemaEscuro


                window.statusBarColor =
                    corBarrasSistema.toArgb()


                window.navigationBarColor =
                    corBarrasSistema.toArgb()
            }


            val scopeTema =
                rememberCoroutineScope()


            BlikTheme(
                modoTema =
                    modoTema
            ) {

                BlikApp(
                    modoRecuperacaoSenha =
                        modoRecuperacaoSenha,

                    onRecuperacaoConcluida = {

                        modoRecuperacaoSenha =
                            false
                    },

                    modoTema =
                        modoTema,

                    onModoTemaAlterado = {
                            novoModo ->

                        scopeTema.launch {

                            preferenciasAppRepository
                                .salvarModoTema(
                                    novoModo
                                )
                        }
                    }
                )
            }
        }
    }


    override fun onNewIntent(
        intent: Intent
    ) {

        super.onNewIntent(
            intent
        )


        setIntent(
            intent
        )


        tratarDeepLink(
            intent
        )
    }


    private fun tratarDeepLink(
        intent: Intent
    ) {

        val uri =
            intent.data
                ?: return


        if (
            uri.scheme != "blik" ||
            uri.host != "auth"
        ) {

            return
        }


        val ehRecuperacao =

            uri.path ==
                    "/recovery" ||

                    uri.fragment
                        ?.contains(
                            "type=recovery",
                            ignoreCase = true
                        ) == true


        if (ehRecuperacao) {

            modoRecuperacaoSenha =
                true
        }


        SupabaseProvider.client
            .handleDeeplinks(
                intent =
                    intent,

                onSessionSuccess = {

                    if (
                        ehRecuperacao
                    ) {

                        runOnUiThread {

                            modoRecuperacaoSenha =
                                true
                        }
                    }
                },

                onError = {

                    if (
                        ehRecuperacao
                    ) {

                        runOnUiThread {

                            modoRecuperacaoSenha =
                                false


                            Toast.makeText(
                                this,
                                "O link de recuperação é inválido ou expirou.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            )
    }
}

private fun mensagemAmigavelLogin(
    erro: Exception
): String {

    val mensagem =
        erro.message
            .orEmpty()
            .lowercase()


    return when {

        "invalid login credentials" in mensagem -> {
            "E-mail ou senha incorretos."
        }


        "email not confirmed" in mensagem -> {
            "Confirme seu e-mail antes de entrar."
        }


        "too many requests" in mensagem ||
                "rate limit" in mensagem -> {
            "Muitas tentativas. Aguarde um pouco e tente novamente."
        }


        "network" in mensagem ||
                "timeout" in mensagem ||
                "unable to resolve host" in mensagem ||
                "failed to connect" in mensagem -> {
            "Não foi possível conectar. Verifique sua internet."
        }


        else -> {
            "Não foi possível entrar. Tente novamente."
        }
    }
}

private fun mensagemAmigavelCadastro(
    erro: Exception
): String {

    val mensagem =
        erro.message
            .orEmpty()
            .lowercase()


    return when {

        "user already registered" in mensagem ||
                "already registered" in mensagem -> {
            "Já existe uma conta cadastrada com este e-mail."
        }


        "password" in mensagem &&
                (
                        "short" in mensagem ||
                                "characters" in mensagem
                        ) -> {
            "A senha não atende aos requisitos mínimos."
        }


        "invalid" in mensagem &&
                "email" in mensagem -> {
            "Digite um e-mail válido."
        }


        "too many requests" in mensagem ||
                "rate limit" in mensagem -> {
            "Muitas tentativas. Aguarde um pouco e tente novamente."
        }


        "network" in mensagem ||
                "timeout" in mensagem ||
                "unable to resolve host" in mensagem ||
                "failed to connect" in mensagem -> {
            "Não foi possível conectar. Verifique sua internet."
        }


        else -> {
            "Não foi possível criar a conta. Tente novamente."
        }
    }
}
@Composable
fun BlikApp(
    modoRecuperacaoSenha: Boolean = false,
    onRecuperacaoConcluida: () -> Unit = {},
    modoTema: ModoTema = ModoTema.SISTEMA,
    onModoTemaAlterado: (ModoTema) -> Unit = {}
) {

    val auth =
        SupabaseProvider.client.auth

    val sessionStatus by
    auth.sessionStatus.collectAsState()

    val scope =
        rememberCoroutineScope()

    val context =
        LocalContext.current

    var carregando by remember {
        mutableStateOf(false)
    }

    var mensagemErro by remember {
        mutableStateOf<String?>(null)
    }

    if (
        modoRecuperacaoSenha
    ) {

        when (
            sessionStatus
        ) {

            is SessionStatus.Authenticated -> {

                TelaNovaSenha(
                    carregando =
                        carregando,

                    mensagemErro =
                        mensagemErro,

                    onSalvar = {
                            novaSenha ->


                        scope.launch {

                            carregando =
                                true

                            mensagemErro =
                                null


                            try {

                                AuthRepository
                                    .atualizarSenha(
                                        novaSenha =
                                            novaSenha
                                    )


                                // Após alterar a senha,
                                // encerra a sessão criada
                                // pelo link de recuperação.

                                try {

                                    AuthRepository
                                        .sair()

                                } catch (
                                    _: Exception
                                ) {

                                    // A senha já foi
                                    // alterada com sucesso.
                                }


                                onRecuperacaoConcluida()


                                Toast.makeText(
                                    context,
                                    "Senha alterada com sucesso. Entre novamente.",
                                    Toast.LENGTH_LONG
                                ).show()


                            } catch (
                                e: Exception
                            ) {

                                mensagemErro =
                                    "Não foi possível alterar a senha. Tente novamente."

                            } finally {

                                carregando =
                                    false
                            }
                        }
                    }
                )
            }


            else -> {

                Box(
                    modifier =
                        Modifier.fillMaxSize(),

                    contentAlignment =
                        Alignment.Center
                ) {

                    CircularProgressIndicator()
                }
            }
        }


        return
    }

    when (sessionStatus) {

        is SessionStatus.Initializing -> {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is SessionStatus.Authenticated -> {

            val usuarioId =
                AuthRepository.usuarioAtualId()

            if (usuarioId == null) {

                Box(
                    modifier =
                        Modifier.fillMaxSize(),
                    contentAlignment =
                        Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

            } else {

                val usuarioValido =
                    remember(usuarioId) {

                        DadosLocaisUsuario
                            .vincularOuValidar(
                                context = context,
                                usuarioId = usuarioId
                            )
                    }


                if (usuarioValido) {

                    AppFinanceiro(
                        modoTema =
                            modoTema,

                        onModoTemaAlterado =
                            onModoTemaAlterado
                    )

                } else {

                    LaunchedEffect(usuarioId) {

                        Toast.makeText(
                            context,
                            "Os dados locais deste aparelho estão vinculados a outra conta.",
                            Toast.LENGTH_LONG
                        ).show()

                        AuthRepository.sair()
                    }

                    Box(
                        modifier =
                            Modifier.fillMaxSize(),
                        contentAlignment =
                            Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }

        is SessionStatus.NotAuthenticated,
        is SessionStatus.RefreshFailure -> {

            TelaAutenticacao(
                carregando = carregando,
                mensagemErro = mensagemErro,

                onEntrar = { email, senha ->

                    scope.launch {

                        carregando = true
                        mensagemErro = null

                        try {

                            AuthRepository.entrar(
                                email = email,
                                senha = senha
                            )

                        } catch (e: Exception) {

                            mensagemErro =
                                mensagemAmigavelLogin(
                                    e
                                )
                        }
                        finally {

                            carregando = false
                        }
                    }
                },

                onRecuperarSenha = { email ->

                    scope.launch {

                        carregando = true
                        mensagemErro = null

                        try {

                            AuthRepository.recuperarSenha(
                                email = email
                            )

                            Toast.makeText(
                                context,
                                "Se este e-mail estiver cadastrado, você receberá um link para redefinir sua senha.",
                                Toast.LENGTH_LONG
                            ).show()

                        } catch (e: Exception) {

                            mensagemErro =
                                "Não foi possível enviar o e-mail de recuperação. Tente novamente."

                        } finally {

                            carregando = false
                        }
                    }
                },

                onCadastrar = { email, senha ->

                    scope.launch {

                        carregando = true
                        mensagemErro = null

                        try {

                            AuthRepository.cadastrar(
                                email = email,
                                senha = senha
                            )

                            if (
                                !AuthRepository.existeSessao()
                            ) {

                                Toast.makeText(
                                    context,
                                    "Cadastro realizado. Verifique seu e-mail para confirmar a conta.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                        } catch (e: Exception) {

                            mensagemErro =
                                mensagemAmigavelCadastro(
                                    e
                                )
                        } finally {

                            carregando = false
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun AppFinanceiro(
    modoTema: ModoTema,
    onModoTemaAlterado: (ModoTema) -> Unit
) {

    val planoAtual =
        PlanoUsuario.PRO

    val context = LocalContext.current

    val banco = remember {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "blik.db"
        )
            .addMigrations(
                MIGRATION_13_14
            )
            .build()
    }

    val dao = banco.movimentacaoDao()
    val contaDao = banco.contaDao()
    val categoriaDao = banco.categoriaDao()
    val cartaoDao = banco.cartaoDao()
    val parcelaCartaoDao = banco.parcelaCartaoDao()
    val pagamentoFaturaDao = banco.pagamentoFaturaDao()


    val parcelasCartao by parcelaCartaoDao
        .listarComDetalhes()
        .collectAsState(initial = emptyList())

    val pagamentosFatura by pagamentoFaturaDao
        .listarTodos()
        .collectAsState(initial = emptyList())

    val pagamentosFaturaComConta by pagamentoFaturaDao
        .listarComConta()
        .collectAsState(initial = emptyList<PagamentoFaturaComConta>())

    val cartoes by cartaoDao
        .listarTodos()
        .collectAsState(initial = emptyList())

    val categorias by categoriaDao
        .listarTodas()
        .collectAsState(initial = emptyList())

    val movimentacoesEntity by dao
        .listarTodas()
        .collectAsState(initial = emptyList())

    val contas by contaDao
        .listarAtivas()
        .collectAsState(initial = emptyList())

    val todasContas by contaDao
        .listarTodas()
        .collectAsState(initial = emptyList())

    val exportarBackupLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.CreateDocument(
                    "application/json"
                )
        ) { uri ->

            if (uri != null) {

                try {

                    val backup =
                        org.json.JSONObject().apply {

                            put("app", "Blik")
                            put("versaoBackup", 1)
                            put(
                                "dataBackup",
                                System.currentTimeMillis()
                            )

                            // =============================================
                            // CONTAS
                            // =============================================

                            put(
                                "contas",
                                org.json.JSONArray().apply {

                                    todasContas.forEach { conta ->

                                        put(
                                            org.json.JSONObject().apply {

                                                put("id", conta.id)

                                                put(
                                                    "nome",
                                                    conta.nome
                                                )

                                                put(
                                                    "saldoInicial",
                                                    conta.saldoInicial
                                                )

                                                put(
                                                    "ativa",
                                                    conta.ativa
                                                )
                                            }
                                        )
                                    }
                                }
                            )


                            // =============================================
                            // CATEGORIAS
                            // =============================================

                            put(
                                "categorias",
                                org.json.JSONArray().apply {

                                    categorias.forEach { categoria ->

                                        put(
                                            org.json.JSONObject().apply {

                                                put(
                                                    "id",
                                                    categoria.id
                                                )

                                                put(
                                                    "nome",
                                                    categoria.nome
                                                )
                                            }
                                        )
                                    }
                                }
                            )


                            // =============================================
                            // CARTÕES
                            // =============================================

                            put(
                                "cartoes",
                                org.json.JSONArray().apply {

                                    cartoes.forEach { cartao ->

                                        put(
                                            org.json.JSONObject().apply {

                                                put(
                                                    "id",
                                                    cartao.id
                                                )

                                                put(
                                                    "nome",
                                                    cartao.nome
                                                )

                                                put(
                                                    "limite",
                                                    cartao.limite
                                                )

                                                put(
                                                    "diaFechamento",
                                                    cartao.diaFechamento
                                                )

                                                put(
                                                    "diaVencimento",
                                                    cartao.diaVencimento
                                                )

                                                put(
                                                    "contaId",
                                                    cartao.contaId
                                                )
                                            }
                                        )
                                    }
                                }
                            )


                            // =============================================
                            // MOVIMENTAÇÕES
                            // =============================================

                            put(
                                "movimentacoes",
                                org.json.JSONArray().apply {

                                    movimentacoesEntity.forEach { movimentacao ->

                                        put(
                                            org.json.JSONObject().apply {

                                                put(
                                                    "id",
                                                    movimentacao.id
                                                )

                                                put(
                                                    "descricao",
                                                    movimentacao.descricao
                                                )

                                                put(
                                                    "valor",
                                                    movimentacao.valor
                                                )

                                                put(
                                                    "tipo",
                                                    movimentacao.tipo
                                                )

                                                put(
                                                    "formaPagamento",
                                                    movimentacao.formaPagamento
                                                        ?: org.json.JSONObject.NULL
                                                )

                                                put(
                                                    "contaId",
                                                    movimentacao.contaId
                                                        ?: org.json.JSONObject.NULL
                                                )

                                                put(
                                                    "contaDestinoId",
                                                    movimentacao.contaDestinoId
                                                        ?: org.json.JSONObject.NULL
                                                )

                                                put(
                                                    "categoriaId",
                                                    movimentacao.categoriaId
                                                        ?: org.json.JSONObject.NULL
                                                )

                                                put(
                                                    "cartaoId",
                                                    movimentacao.cartaoId
                                                        ?: org.json.JSONObject.NULL
                                                )

                                                put(
                                                    "quantidadeParcelas",
                                                    movimentacao.quantidadeParcelas
                                                )

                                                put(
                                                    "data",
                                                    movimentacao.data
                                                )
                                            }
                                        )
                                    }
                                }
                            )


                            // =============================================
                            // PARCELAS
                            // =============================================

                            put(
                                "parcelas",
                                org.json.JSONArray().apply {

                                    parcelasCartao.forEach { parcela ->

                                        put(
                                            org.json.JSONObject().apply {

                                                put(
                                                    "id",
                                                    parcela.id
                                                )

                                                put(
                                                    "movimentacaoId",
                                                    parcela.movimentacaoId
                                                )

                                                put(
                                                    "cartaoId",
                                                    parcela.cartaoId
                                                )

                                                put(
                                                    "numeroParcela",
                                                    parcela.numeroParcela
                                                )

                                                put(
                                                    "totalParcelas",
                                                    parcela.totalParcelas
                                                )

                                                put(
                                                    "valor",
                                                    parcela.valor
                                                )

                                                put(
                                                    "mesFatura",
                                                    parcela.mesFatura
                                                )

                                                put(
                                                    "anoFatura",
                                                    parcela.anoFatura
                                                )

                                                put(
                                                    "quitadaAnteriormente",
                                                    parcela.quitadaAnteriormente
                                                )
                                            }
                                        )
                                    }
                                }
                            )


                            // =============================================
                            // PAGAMENTOS DE FATURA
                            // =============================================

                            put(
                                "pagamentosFatura",
                                org.json.JSONArray().apply {

                                    pagamentosFatura.forEach { pagamento ->

                                        put(
                                            org.json.JSONObject().apply {

                                                put(
                                                    "id",
                                                    pagamento.id
                                                )

                                                put(
                                                    "cartaoId",
                                                    pagamento.cartaoId
                                                )

                                                put(
                                                    "contaId",
                                                    pagamento.contaId
                                                )

                                                put(
                                                    "mesFatura",
                                                    pagamento.mesFatura
                                                )

                                                put(
                                                    "anoFatura",
                                                    pagamento.anoFatura
                                                )

                                                put(
                                                    "valorPago",
                                                    pagamento.valorPago
                                                )

                                                put(
                                                    "dataPagamento",
                                                    pagamento.dataPagamento
                                                )
                                            }
                                        )
                                    }
                                }
                            )
                        }


                    context.contentResolver
                        .openOutputStream(uri)
                        ?.bufferedWriter()
                        ?.use { writer ->

                            writer.write(
                                backup.toString(2)
                            )
                        }


                    android.widget.Toast
                        .makeText(
                            context,
                            "Backup exportado com sucesso.",
                            android.widget.Toast.LENGTH_SHORT
                        )
                        .show()

                } catch (e: Exception) {

                    android.widget.Toast
                        .makeText(
                            context,
                            "Não foi possível exportar o backup.",
                            android.widget.Toast.LENGTH_LONG
                        )
                        .show()
                }
            }
        }

    var backupPendente by remember {
        mutableStateOf<org.json.JSONObject?>(null)
    }

    var mostrarConfirmacaoRestauracao by remember {
        mutableStateOf(false)
    }

    val restaurarBackupLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.OpenDocument()
        ) { uri ->

            if (uri != null) {

                try {

                    val conteudo =
                        context.contentResolver
                            .openInputStream(uri)
                            ?.bufferedReader()
                            ?.use { reader ->
                                reader.readText()
                            }
                            ?: throw Exception(
                                "Não foi possível ler o arquivo."
                            )

                    val backup =
                        org.json.JSONObject(
                            conteudo
                        )

                    val app =
                        backup.optString(
                            "app"
                        )

                    val versaoBackup =
                        backup.optInt(
                            "versaoBackup",
                            -1
                        )

                    val estruturaValida =
                        backup.has("contas") &&
                                backup.has("categorias") &&
                                backup.has("cartoes") &&
                                backup.has("movimentacoes") &&
                                backup.has("parcelas") &&
                                backup.has("pagamentosFatura")

                    if (
                        app == "Blik" &&
                        versaoBackup == 1 &&
                        estruturaValida
                    ) {

                        backupPendente =
                            backup

                        mostrarConfirmacaoRestauracao =
                            true

                    } else {

                        android.widget.Toast
                            .makeText(
                                context,
                                "Este arquivo não é um backup compatível do Blik.",
                                android.widget.Toast.LENGTH_LONG
                            )
                            .show()
                    }

                } catch (e: Exception) {

                    android.widget.Toast
                        .makeText(
                            context,
                            "Não foi possível ler o backup.",
                            android.widget.Toast.LENGTH_LONG
                        )
                        .show()
                }
            }
        }

    LaunchedEffect(Unit) {

        val usuarioId =
            AuthRepository.usuarioAtualId()

        var exclusoesRemotasConcluidas =
            false


        if (usuarioId != null) {

            try {

                val quantidadeExcluida =
                    SyncTombstoneRepository
                        .aplicarNoRoom(

                            contaDao =
                                contaDao,

                            categoriaDao =
                                categoriaDao,

                            cartaoDao =
                                cartaoDao,

                            movimentacaoDao =
                                dao,

                            parcelaCartaoDao =
                                parcelaCartaoDao,

                            pagamentoFaturaDao =
                                pagamentoFaturaDao
                        )


                exclusoesRemotasConcluidas =
                    true


                if (quantidadeExcluida > 0) {

                    Toast.makeText(
                        context,
                        "$quantidadeExcluida registro(s) excluído(s) em outro dispositivo.",
                        Toast.LENGTH_SHORT
                    ).show()
                }


            } catch (e: Exception) {

                Toast.makeText(
                    context,
                    "Não foi possível verificar exclusões na nuvem. " +
                            "A sincronização desta abertura foi adiada.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        if (
            usuarioId == null ||
            !exclusoesRemotasConcluidas
        ) {
            return@LaunchedEffect
        }


        var consultaContasNuvemConcluida =
            false


        if (
            usuarioId != null
        ) {

            try {

                val quantidadeBaixada =
                    ContaSyncRepository.baixarTodasParaRoom(
                        contaDao = contaDao
                    )

                consultaContasNuvemConcluida =
                    true


                if (quantidadeBaixada > 0) {

                    Toast.makeText(
                        context,
                        "$quantidadeBaixada conta(s) baixada(s) da nuvem.",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {

            }
        }

        if (
            contaDao.quantidade() == 0 &&
            consultaContasNuvemConcluida
        ) {

            contaDao.inserirTodas(
                listOf(
                    ContaEntity(
                        nome = "Banco do Brasil"
                    )
                )
            )
        }

        // =============================================
        // SUPABASE -> ROOM
        // BAIXA CATEGORIAS EM UM DISPOSITIVO NOVO
        // =============================================

        var consultaCategoriasNuvemConcluida =
            false

        if (
            usuarioId != null
        ) {

            try {

                val quantidadeBaixada =
                    CategoriaSyncRepository.baixarTodasParaRoom(
                        categoriaDao = categoriaDao
                    )

                consultaCategoriasNuvemConcluida =
                    true

                if (quantidadeBaixada > 0) {

                    Toast.makeText(
                        context,
                        "$quantidadeBaixada categoria(s) baixada(s) da nuvem.",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {

                Toast.makeText(
                    context,
                    "ERRO CATEGORIAS: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // Só cria as categorias padrão quando a consulta à nuvem
        // terminou corretamente e não havia categorias remotas.
        if (
            categoriaDao.quantidade() == 0 &&
            consultaCategoriasNuvemConcluida
        ) {

            categoriaDao.inserirTodas(
                listOf(
                    CategoriaEntity(nome = "Alimentação"),
                    CategoriaEntity(nome = "Assinaturas"),
                    CategoriaEntity(nome = "Combustível"),
                    CategoriaEntity(nome = "Compras"),
                    CategoriaEntity(nome = "Saúde"),
                    CategoriaEntity(nome = "Viagem"),
                    CategoriaEntity(nome = "Outros")
                )
            )
        }


// =============================================
// SUPABASE -> ROOM
// BAIXA CARTÕES EM UM DISPOSITIVO NOVO
// =============================================

        if (
            usuarioId != null
        ) {

            try {

                val quantidadeBaixada =
                    CartaoSyncRepository.baixarTodosParaRoom(
                        cartaoDao = cartaoDao,
                        contaDao = contaDao
                    )


                if (quantidadeBaixada > 0) {

                    Toast.makeText(
                        context,
                        "$quantidadeBaixada cartão(ões) baixado(s) da nuvem.",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {

                Toast.makeText(
                    context,
                    "ERRO CARTÕES: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // =============================================
// SUPABASE -> ROOM
// BAIXA MOVIMENTAÇÕES EM UM DISPOSITIVO NOVO
// =============================================

        if (
            usuarioId != null
        ) {

            try {

                val quantidadeBaixada =
                    MovimentacaoSyncRepository.baixarTodasParaRoom(
                        movimentacaoDao = dao,
                        contaDao = contaDao,
                        categoriaDao = categoriaDao,
                        cartaoDao = cartaoDao
                    )


                if (quantidadeBaixada > 0) {

                    Toast.makeText(
                        context,
                        "$quantidadeBaixada movimentação(ões) baixada(s) da nuvem.",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {

                Toast.makeText(
                    context,
                    "Não foi possível baixar as movimentações: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // =============================================
// SUPABASE -> ROOM
// BAIXA PARCELAS EM UM DISPOSITIVO NOVO
// =============================================

        if (
            usuarioId != null
        ) {

            try {

                val quantidadeBaixada =
                    ParcelaCartaoSyncRepository
                        .baixarTodasParaRoom(
                            parcelaCartaoDao =
                                parcelaCartaoDao,

                            movimentacaoDao =
                                dao,

                            cartaoDao =
                                cartaoDao
                        )


                if (quantidadeBaixada > 0) {

                    Toast.makeText(
                        context,
                        "$quantidadeBaixada parcela(s) baixada(s) da nuvem.",
                        Toast.LENGTH_LONG
                    ).show()
                }


            } catch (e: Exception) {

                Toast.makeText(
                    context,
                    "Não foi possível baixar as parcelas: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // =============================================
// SUPABASE -> ROOM
// BAIXA PAGAMENTOS EM UM DISPOSITIVO NOVO
// =============================================

        if (
            usuarioId != null
        ) {

            try {

                val quantidadeBaixada =
                    PagamentoFaturaSyncRepository
                        .baixarTodosParaRoom(
                            pagamentoFaturaDao =
                                pagamentoFaturaDao,

                            contaDao =
                                contaDao,

                            cartaoDao =
                                cartaoDao
                        )


                if (quantidadeBaixada > 0) {

                    Toast.makeText(
                        context,
                        "$quantidadeBaixada pagamento(s) baixado(s) da nuvem.",
                        Toast.LENGTH_LONG
                    ).show()
                }


            } catch (e: Exception) {

                Toast.makeText(
                    context,
                    "Não foi possível baixar os pagamentos: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }


        val contasAtualizadas =
            SyncIdRepository.preencherContas(
                contaDao
            )

        if (contasAtualizadas > 0) {

            Toast.makeText(
                context,
                "$contasAtualizadas conta(s) preparada(s) para sincronização.",
                Toast.LENGTH_LONG
            ).show()
        }

        val categoriasAtualizadas =
            SyncIdRepository.preencherCategorias(
                categoriaDao
            )

        val cartoesAtualizados =
            SyncIdRepository.preencherCartoes(
                cartaoDao
            )

        val movimentacoesAtualizadas =
            SyncIdRepository.preencherMovimentacoes(
                dao
            )

        val parcelasAtualizadas =
            SyncIdRepository.preencherParcelas(
                parcelaCartaoDao
            )

        val pagamentosAtualizados =
            SyncIdRepository.preencherPagamentos(
                pagamentoFaturaDao
            )

        val totalAtualizado =
            categoriasAtualizadas +
                    cartoesAtualizados +
                    movimentacoesAtualizadas +
                    parcelasAtualizadas +
                    pagamentosAtualizados

        if (totalAtualizado > 0) {
            Toast.makeText(
                context,
                """
        Preparados para sincronização:
        Categorias: $categoriasAtualizadas
        Cartões: $cartoesAtualizados
        Movimentações: $movimentacoesAtualizadas
        Parcelas: $parcelasAtualizadas
        Pagamentos: $pagamentosAtualizados
        """.trimIndent(),
                Toast.LENGTH_LONG
            ).show()
        }

        if (usuarioId != null) {

            try {

                val contasParaSincronizar =
                    contaDao.listarTodasUmaVez()

                ContaSyncRepository.sincronizarTodas(
                    contas = contasParaSincronizar,
                    usuarioId = usuarioId
                )
            } catch (e: Exception) {

            }

            try {

                val categoriasParaSincronizar = categoriaDao.listarTodasUmaVez()
                CategoriaSyncRepository.sincronizarTodas(
                    categorias = categoriasParaSincronizar,
                    usuarioId = usuarioId
                )
            } catch (e: Exception) {

            }
            try {

                val cartoesParaSincronizar =
                    cartaoDao.listarTodosUmaVez()

                val contasParaRelacionamento =
                    contaDao.listarTodasUmaVez()


                CartaoSyncRepository.sincronizarTodos(
                    cartoes = cartoesParaSincronizar,
                    contas = contasParaRelacionamento,
                    usuarioId = usuarioId
                )

            } catch (e: Exception) {

            }

            try {

                val movimentacoesParaSincronizar =
                    dao.listarTodasUmaVez()

                val contasParaRelacionamento =
                    contaDao.listarTodasUmaVez()

                val categoriasParaRelacionamento =
                    categoriaDao.listarTodasUmaVez()

                val cartoesParaRelacionamento =
                    cartaoDao.listarTodosUmaVez()


                MovimentacaoSyncRepository.sincronizarTodas(
                    movimentacoes = movimentacoesParaSincronizar,
                    contas = contasParaRelacionamento,
                    categorias = categoriasParaRelacionamento,
                    cartoes = cartoesParaRelacionamento,
                    usuarioId = usuarioId
                )

            } catch (e: Exception) {

            }

            // =============================================
// ROOM -> SUPABASE
// PARCELAS
// =============================================

            try {

                val parcelasParaSincronizar =
                    parcelaCartaoDao.listarTodasUmaVez()

                val movimentacoesParaRelacionamento =
                    dao.listarTodasUmaVez()

                val cartoesParaRelacionamento =
                    cartaoDao.listarTodosUmaVez()


                ParcelaCartaoSyncRepository.sincronizarTodos(
                    parcelas =
                        parcelasParaSincronizar,

                    movimentacoes =
                        movimentacoesParaRelacionamento,

                    cartoes =
                        cartoesParaRelacionamento,

                    usuarioId =
                        usuarioId
                )

            } catch (e: Exception) {

                Toast.makeText(
                    context,
                    "Não foi possível sincronizar as parcelas: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }


// =============================================
// ROOM -> SUPABASE
// PAGAMENTOS
// =============================================

            try {

                val pagamentosParaSincronizar =
                    pagamentoFaturaDao.listarTodosUmaVez()

                val contasParaRelacionamento =
                    contaDao.listarTodasUmaVez()

                val cartoesParaRelacionamento =
                    cartaoDao.listarTodosUmaVez()


                PagamentoFaturaSyncRepository.sincronizarTodos(
                    pagamentos =
                        pagamentosParaSincronizar,

                    contas =
                        contasParaRelacionamento,

                    cartoes =
                        cartoesParaRelacionamento,

                    usuarioId =
                        usuarioId
                )

            } catch (e: Exception) {

                Toast.makeText(
                    context,
                    "Não foi possível sincronizar os pagamentos: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    }

    val scope = rememberCoroutineScope()

    var telaAtual by remember {
        mutableStateOf("inicio")
    }

    var faturaInicialCartaoId by remember {
        mutableStateOf<Int?>(null)
    }

    var faturaInicialMes by remember {
        mutableStateOf<Int?>(null)
    }

    var faturaInicialAno by remember {
        mutableStateOf<Int?>(null)
    }

    var movimentacaoEmEdicao by remember {
        mutableStateOf<Movimentacao?>(null)
    }

    val movimentacoes =
        movimentacoesEntity.map { item ->

            Movimentacao(
                id = item.id,
                descricao = item.descricao,
                valor = item.valor,
                tipo = item.tipo,
                formaPagamento = item.formaPagamento,

                contaId = item.contaId,
                contaNome = item.contaNome,

                contaDestinoId = item.contaDestinoId,
                contaDestinoNome = item.contaDestinoNome,

                categoriaId = item.categoriaId,
                categoriaNome = item.categoriaNome,

                cartaoId = item.cartaoId,
                cartaoNome = item.cartaoNome,

                quantidadeParcelas =
                    item.quantidadeParcelas,

                data = item.data
            )
        }

    when (telaAtual) {

        "inicio" -> {
            TelaInicial(
                movimentacoes = movimentacoes,
                contas = contas,
                pagamentosFatura = pagamentosFatura,
                parcelasCartao = parcelasCartao,
                pagamentosFaturaComConta = pagamentosFaturaComConta,
                cartoes = cartoes,


                onNovaMovimentacao = {
                    telaAtual = "nova_movimentacao"
                },
                onContas = {
                    telaAtual = "contas"
                },
                onCartoes = {
                    telaAtual = "cartoes"
                },
                onCategorias = {
                    telaAtual = "categorias"
                },
                onFaturas = {
                    faturaInicialCartaoId = null
                    faturaInicialMes = null
                    faturaInicialAno = null

                    telaAtual = "faturas"
                },

                onAbrirFatura = {
                    cartaoId,
                        mes,
                        ano ->
                    faturaInicialCartaoId = cartaoId
                    faturaInicialMes = mes
                    faturaInicialAno = ano
                    telaAtual = "faturas"
                },

                onHistorico = {
                    telaAtual = "historico"
                },

                onExportarBackup = {
                    exportarBackupLauncher.launch(
                        "blik-backup.json"
                    )
                },

                onRestaurarBackup = {

                    restaurarBackupLauncher.launch(
                        arrayOf(
                            "application/json",
                            "text/json",
                            "text/plain"
                        )
                    )
                },

                onConfiguracoes = {
                    telaAtual = "configuracoes"
                },

                onSair = {

                    scope.launch {

                        try {

                            AuthRepository.sair()

                        } catch (e: Exception) {

                            Toast.makeText(
                                context,
                                "Não foi possível sair da conta.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            )
        }

        "cartoes" -> {
            TelaCartoes(
                cartoes = cartoes,
                contas = contas,
                parcelasCartao = parcelasCartao,
                pagamentosFatura = pagamentosFatura,

                onAdicionar ={
                        nome,
                        limite,
                        diaFechamento,
                        diaVencimento,
                        contaId,
                        resultado ->
                    scope.launch {
                        val nomeLimpo = nome.trim()
                        val existe =
                            cartaoDao.existeNome(nomeLimpo) > 0
                        if (existe) {
                            resultado(false)
                        } else {
                            val novoCartao =
                                CartaoEntity(
                                    nome = nomeLimpo,
                                    limite = limite,
                                    diaFechamento = diaFechamento,
                                    diaVencimento = diaVencimento,
                                    contaId = contaId
                                )
                            cartaoDao.inserir(novoCartao)

                            val usuarioId = AuthRepository.usuarioAtualId()
                            if (usuarioId != null) {
                                try {
                                    val contasParaRelacionamento =
                                        contaDao.listarTodasUmaVez()

                                    CartaoSyncRepository.sincronizar(
                                        cartao = novoCartao,
                                        contas = contasParaRelacionamento,
                                        usuarioId = usuarioId
                                    )
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        "Cartão salvo no aparelho, mas ainda não foi sincronizado.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }

                            resultado(true)
                        }
                    }
                },

                onEditar = {
                        cartao,
                        novoNome,
                        novoLimite,
                        novoDiaFechamento,
                        novoDiaVencimento,
                        novaContaId,
                        resultado ->

                    scope.launch {

                        val nomeLimpo =
                            novoNome.trim()

                        val existe =
                            cartaoDao.existeOutroNome(
                                nome = nomeLimpo,
                                idAtual = cartao.id
                            ) > 0

                        if (existe) {

                            resultado(false)

                        } else {

                            // Primeiro salva no Room
                            cartaoDao.editar(
                                id = cartao.id,
                                nome = nomeLimpo,
                                limite = novoLimite,
                                diaFechamento = novoDiaFechamento,
                                diaVencimento = novoDiaVencimento,
                                contaId = novaContaId
                            )


                            // Depois tenta sincronizar com o Supabase
                            val usuarioId =
                                AuthRepository.usuarioAtualId()

                            if (usuarioId != null) {

                                try {

                                    val cartaoAtualizado =
                                        cartaoDao
                                            .listarTodosUmaVez()
                                            .firstOrNull { item ->
                                                item.id == cartao.id
                                            }
                                            ?: throw IllegalStateException(
                                                "Cartão atualizado não encontrado."
                                            )


                                    val contasParaRelacionamento =
                                        contaDao.listarTodasUmaVez()


                                    CartaoSyncRepository.sincronizar(
                                        cartao = cartaoAtualizado,
                                        contas = contasParaRelacionamento,
                                        usuarioId = usuarioId
                                    )

                                } catch (e: Exception) {

                                    Toast.makeText(
                                        context,
                                        "Alteração salva no aparelho, mas ainda não foi sincronizada.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }


                            resultado(true)
                        }
                    }
                },
                onExcluir = { cartao, resultado ->

                    scope.launch {

                        val quantidadeMovimentacoes =
                            movimentacoesEntity.count { movimentacao ->
                                movimentacao.cartaoId == cartao.id
                            }

                        val quantidadeParcelas =
                            parcelasCartao.count { parcela ->
                                parcela.cartaoId == cartao.id
                            }

                        val quantidadePagamentos =
                            pagamentosFatura.count { pagamento ->
                                pagamento.cartaoId == cartao.id
                            }


                        if (
                            quantidadeMovimentacoes > 0 ||
                            quantidadeParcelas > 0 ||
                            quantidadePagamentos > 0
                        ) {

                            resultado(
                                false,
                                "Não é possível excluir o cartão \"${cartao.nome}\" porque existem registros financeiros vinculados a ele."
                            )

                        } else {

                            try {

                                val cartaoLocal =
                                    cartaoDao
                                        .listarTodosUmaVez()
                                        .firstOrNull { item ->
                                            item.id == cartao.id
                                        }
                                        ?: throw IllegalStateException(
                                            "Cartão local não encontrado."
                                        )


                                val syncId =
                                    cartaoLocal.syncId
                                        ?: throw IllegalStateException(
                                            "O cartão não possui syncId."
                                        )


                                // Primeiro Supabase
                                CartaoSyncRepository.excluir(
                                    syncId = syncId
                                )


                                // Depois Room
                                cartaoDao.excluir(
                                    cartao.id
                                )


                                resultado(
                                    true,
                                    ""
                                )

                            } catch (
                                e: android.database.sqlite.SQLiteConstraintException
                            ) {

                                resultado(
                                    false,
                                    "Não é possível excluir o cartão \"${cartao.nome}\" porque ainda existem registros vinculados a ele."
                                )

                            } catch (e: Exception) {

                                resultado(
                                    false,
                                    "Não foi possível excluir o cartão: ${e.message}"
                                )
                            }
                        }
                    }
                },

                onVoltar = {
                    telaAtual = "inicio"
                }
            )
        }

        "faturas" -> {
            TelaFaturas(
                parcelas = parcelasCartao,
                pagamentos = pagamentosFaturaComConta,
                contas = contas,
                cartoes = cartoes,
                mesInicial = faturaInicialMes,
                anoInicial = faturaInicialAno,
                cartaoInicialExpandidoId = faturaInicialCartaoId,

                onPagar = {
                        cartaoId,
                        contaId,
                        mes,
                        ano,
                        valor,
                        data ->

                    scope.launch {

                        val novoPagamento =
                            PagamentoFaturaEntity(
                                cartaoId =
                                    cartaoId,

                                contaId =
                                    contaId,

                                mesFatura =
                                    mes,

                                anoFatura =
                                    ano,

                                valorPago =
                                    valor,

                                dataPagamento =
                                    data
                            )


                        // Primeiro salva localmente.
                        pagamentoFaturaDao.inserir(
                            novoPagamento
                        )


                        // Depois tenta sincronizar.
                        val usuarioId =
                            AuthRepository.usuarioAtualId()

                        if (usuarioId != null) {

                            try {

                                val contasParaRelacionamento =
                                    contaDao.listarTodasUmaVez()

                                val cartoesParaRelacionamento =
                                    cartaoDao.listarTodosUmaVez()


                                PagamentoFaturaSyncRepository.sincronizar(
                                    pagamento =
                                        novoPagamento,

                                    contas =
                                        contasParaRelacionamento,

                                    cartoes =
                                        cartoesParaRelacionamento,

                                    usuarioId =
                                        usuarioId
                                )

                            } catch (e: Exception) {

                                Toast.makeText(
                                    context,
                                    "Pagamento salvo no aparelho, mas ainda não foi sincronizado.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                },
                onExcluirPagamento = { pagamento ->

                    scope.launch {

                        try {

                            // =============================================
                            // LOCALIZA A ENTITY ORIGINAL NO ROOM
                            // PARA OBTER O syncId
                            // =============================================

                            val pagamentoLocal =
                                pagamentoFaturaDao
                                    .listarTodosUmaVez()
                                    .firstOrNull { item ->
                                        item.id == pagamento.id
                                    }
                                    ?: throw IllegalStateException(
                                        "Pagamento local não encontrado."
                                    )


                            val syncId =
                                pagamentoLocal.syncId
                                    ?: throw IllegalStateException(
                                        "O pagamento não possui syncId."
                                    )


                            // =============================================
                            // PRIMEIRO EXCLUI DO SUPABASE
                            // =============================================

                            PagamentoFaturaSyncRepository.excluir(
                                syncId = syncId
                            )


                            // =============================================
                            // SOMENTE DEPOIS EXCLUI DO ROOM
                            // =============================================

                            pagamentoFaturaDao.excluir(
                                pagamento.id
                            )


                            Toast.makeText(
                                context,
                                "Pagamento excluído.",
                                Toast.LENGTH_SHORT
                            ).show()


                        } catch (e: Exception) {

                            Toast.makeText(
                                context,
                                "Não foi possível excluir o pagamento: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                },

                onVoltar = {
                    telaAtual = "inicio"
                }
            )
        }

        "categorias" -> {

            TelaCategorias(
                categorias = categorias,
                onAdicionar = { nome, resultado ->
                    scope.launch {
                        val nomeLimpo = nome.trim()
                        val existe =
                            categoriaDao.existeNome(nomeLimpo) > 0
                        if (existe) {
                            resultado(false)
                        } else {
                            val novaCategoria = CategoriaEntity(
                                nome = nomeLimpo
                            )
                            categoriaDao.inserir(novaCategoria)

                            val usuarioId = AuthRepository.usuarioAtualId()
                            if (usuarioId != null) {
                                try {
                                    CategoriaSyncRepository.sincronizar(
                                        categoria = novaCategoria,
                                        usuarioId = usuarioId
                                    )
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        "Categoria salva no aparelho, mas ainda não foi sincronizada.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                            resultado(true)
                        }
                    }
                },

                onEditar = { categoria, novoNome, resultado ->
                    scope.launch {
                        val nomeLimpo = novoNome.trim()
                        val existe =
                            categoriaDao.existeOutroNome(
                                nome = nomeLimpo,
                                idAtual = categoria.id
                            ) > 0
                        if (existe) {
                            resultado(false)
                        } else {
                            categoriaDao.editarNome(
                                id = categoria.id,
                                novoNome = nomeLimpo
                            )
                            val categoriaAtualizada = categoria.copy(
                                nome = nomeLimpo
                            )
                            val usuarioId = AuthRepository.usuarioAtualId()

                            if (usuarioId != null) {
                                try {
                                    CategoriaSyncRepository.sincronizar(
                                        categoria = categoriaAtualizada,
                                        usuarioId = usuarioId
                                    )
                                } catch(e: Exception) {
                                    Toast.makeText(
                                        context,
                                        "Alteração salva no aparelho, mas ainda não foi sincronizada.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                            resultado(true)
                        }
                    }
                },

                onExcluir = { categoria, resultado ->

                    scope.launch {

                        val quantidade =
                            dao.quantidadePorCategoria(
                                categoria.id
                            )


                        if (quantidade > 0) {

                            resultado(
                                false,
                                quantidade
                            )

                        } else {

                            try {

                                val categoriaLocal =
                                    categoriaDao
                                        .listarTodasUmaVez()
                                        .firstOrNull { item ->
                                            item.id == categoria.id
                                        }
                                        ?: throw IllegalStateException(
                                            "Categoria local não encontrada."
                                        )


                                val syncId =
                                    categoriaLocal.syncId
                                        ?: throw IllegalStateException(
                                            "A categoria não possui syncId."
                                        )


                                // Primeiro Supabase
                                CategoriaSyncRepository.excluir(
                                    syncId = syncId
                                )


                                // Depois Room
                                categoriaDao.excluir(
                                    categoria.id
                                )


                                resultado(
                                    true,
                                    0
                                )

                            } catch (e: Exception) {

                                Toast.makeText(
                                    context,
                                    "Não foi possível excluir a categoria: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                },

                onVoltar = {
                    telaAtual = "inicio"
                }
            )
        }

        "nova_movimentacao" -> {
            TelaNovaMovimentacao(
                contas = contas,
                categorias = categorias,
                cartoes = cartoes,

                onSalvar = { novaMovimentacao ->

                    scope.launch {

                        // =============================================
                        // CRIA A MOVIMENTAÇÃO LOCAL
                        // =============================================

                        val novaMovimentacaoEntity =
                            MovimentacaoEntity(
                                descricao =
                                    novaMovimentacao.descricao,

                                valor =
                                    novaMovimentacao.valor,

                                tipo =
                                    novaMovimentacao.tipo,

                                formaPagamento =
                                    novaMovimentacao.formaPagamento,

                                contaId =
                                    novaMovimentacao.contaId,

                                contaDestinoId =
                                    novaMovimentacao.contaDestinoId,

                                categoriaId =
                                    novaMovimentacao.categoriaId,

                                cartaoId =
                                    novaMovimentacao.cartaoId,

                                quantidadeParcelas =
                                    novaMovimentacao.quantidadeParcelas,

                                data =
                                    novaMovimentacao.data
                            )


                        val idMovimentacao =
                            dao.inserir(
                                novaMovimentacaoEntity
                            )


                        // =============================================
                        // GERA PARCELAS SE FOR COMPRA NO CRÉDITO
                        // =============================================

                        if (
                            novaMovimentacao.tipo == "Saída" &&
                            novaMovimentacao.formaPagamento == "Crédito"
                        ) {

                            val cartaoId =
                                novaMovimentacao.cartaoId

                            val cartao =
                                cartoes.find {
                                    it.id == cartaoId
                                }

                            if (
                                cartaoId != null &&
                                cartao != null
                            ) {

                                val parcelas =
                                    gerarParcelasCartao(
                                        movimentacaoId =
                                            idMovimentacao.toInt(),

                                        cartaoId =
                                            cartaoId,

                                        valorTotal =
                                            novaMovimentacao.valor,

                                        quantidadeParcelas =
                                            novaMovimentacao.quantidadeParcelas,

                                        dataCompra =
                                            novaMovimentacao.data,

                                        diaFechamento =
                                            cartao.diaFechamento,

                                        diaVencimento =
                                            cartao.diaVencimento
                                    )

                                parcelaCartaoDao
                                    .inserirTodas(
                                        parcelas
                                    )
                            }
                        }


                        // =============================================
                        // TENTA SINCRONIZAR A MOVIMENTAÇÃO
                        // =============================================

                        val usuarioId =
                            AuthRepository.usuarioAtualId()

                        if (usuarioId != null) {

                            try {

                                val contasParaRelacionamento =
                                    contaDao.listarTodasUmaVez()

                                val categoriasParaRelacionamento =
                                    categoriaDao.listarTodasUmaVez()

                                val cartoesParaRelacionamento =
                                    cartaoDao.listarTodosUmaVez()


                                // =============================================
                                // SINCRONIZA PRIMEIRO A MOVIMENTAÇÃO
                                // =============================================

                                MovimentacaoSyncRepository.sincronizar(
                                    movimentacao =
                                        novaMovimentacaoEntity,

                                    contas =
                                        contasParaRelacionamento,

                                    categorias =
                                        categoriasParaRelacionamento,

                                    cartoes =
                                        cartoesParaRelacionamento,

                                    usuarioId =
                                        usuarioId
                                )


                                // =============================================
                                // SINCRONIZA AS PARCELAS DA MOVIMENTAÇÃO
                                // =============================================

                                val parcelasParaSincronizar =
                                    parcelaCartaoDao
                                        .listarTodasUmaVez()
                                        .filter { parcela ->

                                            parcela.movimentacaoId ==
                                                    idMovimentacao.toInt()
                                        }


                                if (parcelasParaSincronizar.isNotEmpty()) {

                                    val movimentacoesParaRelacionamento =
                                        dao.listarTodasUmaVez()


                                    ParcelaCartaoSyncRepository
                                        .sincronizarTodos(
                                            parcelas =
                                                parcelasParaSincronizar,

                                            movimentacoes =
                                                movimentacoesParaRelacionamento,

                                            cartoes =
                                                cartoesParaRelacionamento,

                                            usuarioId =
                                                usuarioId
                                        )
                                }

                            } catch (e: Exception) {

                                Toast.makeText(
                                    context,
                                    "Movimentação salva no aparelho, mas ainda não foi sincronizada.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }

                        telaAtual = "inicio"
                    }
                },

                onVoltar = {
                    telaAtual = "inicio"
                }
            )
        }

        "editar_movimentacao" -> {

            TelaNovaMovimentacao(
                contas = contas,
                categorias = categorias,
                cartoes = cartoes,
                movimentacaoParaEditar = movimentacaoEmEdicao,

                onSalvar = { movimentacao ->

                    scope.launch {

                        // =============================================
                        // GUARDA AS PARCELAS ANTIGAS
                        // ANTES DE ALTERAR QUALQUER COISA
                        // =============================================

                        val parcelasAntigas =
                            parcelaCartaoDao
                                .listarTodasUmaVez()
                                .filter { parcela ->
                                    parcela.movimentacaoId ==
                                            movimentacao.id
                                }


                        // =============================================
                        // PRESERVA O HISTÓRICO DAS PARCELAS
                        // =============================================

                        val quitadasAnteriormentePreservadas =
                            parcelasAntigas
                                .filter { parcela ->
                                    parcela.quitadaAnteriormente
                                }
                                .map { parcela ->
                                    parcela.numeroParcela
                                }
                                .toSet()


                        // =============================================
                        // EDITA A MOVIMENTAÇÃO NO ROOM
                        // =============================================

                        dao.editar(
                            id =
                                movimentacao.id,

                            descricao =
                                movimentacao.descricao,

                            valor =
                                movimentacao.valor,

                            tipo =
                                movimentacao.tipo,

                            formaPagamento =
                                movimentacao.formaPagamento,

                            contaId =
                                movimentacao.contaId,

                            contaDestinoId =
                                movimentacao.contaDestinoId,

                            categoriaId =
                                movimentacao.categoriaId,

                            cartaoId =
                                movimentacao.cartaoId,

                            quantidadeParcelas =
                                movimentacao.quantidadeParcelas,

                            data =
                                movimentacao.data
                        )


                        // =============================================
                        // REMOVE AS PARCELAS LOCAIS ANTIGAS
                        // =============================================

                        parcelaCartaoDao
                            .excluirPorMovimentacao(
                                movimentacao.id
                            )


                        // =============================================
                        // PREPARA AS NOVAS PARCELAS
                        // =============================================

                        var parcelasNovas =
                            emptyList<ParcelaCartaoEntity>()


                        if (
                            movimentacao.tipo == "Saída" &&
                            movimentacao.formaPagamento == "Crédito"
                        ) {

                            val cartaoId =
                                movimentacao.cartaoId

                            val cartao =
                                cartoes.find { item ->
                                    item.id == cartaoId
                                }


                            if (
                                cartaoId != null &&
                                cartao != null
                            ) {

                                val parcelasGeradas =
                                    gerarParcelasCartao(
                                        movimentacaoId =
                                            movimentacao.id,

                                        cartaoId =
                                            cartaoId,

                                        valorTotal =
                                            movimentacao.valor,

                                        quantidadeParcelas =
                                            movimentacao.quantidadeParcelas,

                                        dataCompra =
                                            movimentacao.data,

                                        diaFechamento =
                                            cartao.diaFechamento,

                                        diaVencimento =
                                            cartao.diaVencimento,

                                        quitadasAnteriormentePreservadas =
                                            quitadasAnteriormentePreservadas
                                    )


                                // =============================================
                                // PRESERVA O syncId DAS PARCELAS EXISTENTES
                                // =============================================

                                parcelasNovas =
                                    parcelasGeradas.map { parcelaNova ->

                                        val parcelaAntiga =
                                            parcelasAntigas
                                                .firstOrNull { antiga ->

                                                    antiga.numeroParcela ==
                                                            parcelaNova.numeroParcela
                                                }


                                        if (
                                            parcelaAntiga?.syncId != null
                                        ) {

                                            parcelaNova.copy(
                                                syncId =
                                                    parcelaAntiga.syncId
                                            )

                                        } else {

                                            // Parcela realmente nova.
                                            // Mantém o UUID criado pelo Entity.
                                            parcelaNova
                                        }
                                    }


                                parcelaCartaoDao
                                    .inserirTodas(
                                        parcelasNovas
                                    )
                            }
                        }


                        // =============================================
                        // SINCRONIZA COM O SUPABASE
                        // =============================================

                        val usuarioId =
                            AuthRepository.usuarioAtualId()


                        if (usuarioId != null) {

                            try {

                                // =============================================
                                // MOVIMENTAÇÃO ATUALIZADA
                                // =============================================

                                val movimentacaoAtualizada =
                                    dao
                                        .listarTodasUmaVez()
                                        .firstOrNull { item ->
                                            item.id ==
                                                    movimentacao.id
                                        }
                                        ?: throw IllegalStateException(
                                            "Movimentação atualizada não encontrada."
                                        )


                                val contasParaRelacionamento =
                                    contaDao.listarTodasUmaVez()

                                val categoriasParaRelacionamento =
                                    categoriaDao.listarTodasUmaVez()

                                val cartoesParaRelacionamento =
                                    cartaoDao.listarTodosUmaVez()


                                // =============================================
                                // 1. ATUALIZA A MOVIMENTAÇÃO
                                // =============================================

                                MovimentacaoSyncRepository.sincronizar(
                                    movimentacao =
                                        movimentacaoAtualizada,

                                    contas =
                                        contasParaRelacionamento,

                                    categorias =
                                        categoriasParaRelacionamento,

                                    cartoes =
                                        cartoesParaRelacionamento,

                                    usuarioId =
                                        usuarioId
                                )


                                // =============================================
                                // 2. ATUALIZA/CRIA AS PARCELAS ATUAIS
                                // =============================================

                                if (
                                    parcelasNovas.isNotEmpty()
                                ) {

                                    val movimentacoesParaRelacionamento =
                                        dao.listarTodasUmaVez()


                                    ParcelaCartaoSyncRepository
                                        .sincronizarTodos(
                                            parcelas =
                                                parcelasNovas,

                                            movimentacoes =
                                                movimentacoesParaRelacionamento,

                                            cartoes =
                                                cartoesParaRelacionamento,

                                            usuarioId =
                                                usuarioId
                                        )
                                }


                                // =============================================
                                // 3. DESCOBRE PARCELAS QUE DEIXARAM DE EXISTIR
                                // =============================================

                                val syncIdsAtuais =
                                    parcelasNovas
                                        .mapNotNull { parcela ->
                                            parcela.syncId
                                        }
                                        .toSet()


                                val parcelasParaExcluir =
                                    parcelasAntigas
                                        .filter { parcelaAntiga ->

                                            val syncIdAntigo =
                                                parcelaAntiga.syncId

                                            syncIdAntigo != null &&
                                                    syncIdAntigo !in
                                                    syncIdsAtuais
                                        }


                                // =============================================
                                // 4. EXCLUI AS PARCELAS OBSOLETAS DA NUVEM
                                // =============================================

                                parcelasParaExcluir
                                    .forEach { parcelaAntiga ->

                                        val syncId =
                                            parcelaAntiga.syncId
                                                ?: return@forEach


                                        ParcelaCartaoSyncRepository
                                            .excluir(
                                                syncId =
                                                    syncId
                                            )
                                    }


                            } catch (e: Exception) {

                                Toast.makeText(
                                    context,
                                    "Alteração salva no aparelho, mas ainda não foi sincronizada: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }


                        // =============================================
                        // FINALIZA A EDIÇÃO
                        // =============================================

                        movimentacaoEmEdicao =
                            null

                        telaAtual =
                            "historico"
                    }
                },

                onVoltar = {

                    movimentacaoEmEdicao =
                        null

                    telaAtual =
                        "historico"
                }
            )
        }

        "contas" -> {
            TelaContas(
                contas = todasContas,

                onAdicionarConta = { nome, saldoInicial, resultado ->
                    scope.launch {
                        val nomeLimpo =
                            nome.trim()

                        val existe =
                            contaDao.existeNome(nomeLimpo) > 0

                        if (existe) {
                            resultado(false)
                        } else {

                            val novaConta =
                                ContaEntity(
                                    nome = nomeLimpo,
                                    saldoInicial = saldoInicial
                                )

                            contaDao.inserir(
                                novaConta
                            )

                            val usuarioId =
                                AuthRepository.usuarioAtualId()

                            if (usuarioId != null) {

                                try {

                                    ContaSyncRepository.sincronizar(
                                        conta = novaConta,
                                        usuarioId = usuarioId
                                    )

                                } catch (e: Exception) {

                                    Toast.makeText(
                                        context,
                                        "Conta salva no aparelho, mas ainda não foi sincronizada.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }

                            resultado(true)
                        }
                    }
                },

                onEditar = { conta, novoNome, novoSaldoInicial, resultado ->
                    scope.launch {
                        val nomeLimpo = novoNome.trim()
                        val existe =
                            contaDao.existeOutroNome(
                                nome = nomeLimpo,
                                idAtual = conta.id
                            ) > 0
                        if (existe) {
                            resultado(false)
                        } else {
                            contaDao.editar(
                                id = conta.id,
                                novoNome = nomeLimpo,
                                novoSaldoInicial = novoSaldoInicial
                            )

                            val contaAtualizada =
                                conta.copy(
                                    nome = nomeLimpo,
                                    saldoInicial = novoSaldoInicial
                                )

                            val usuarioId =
                                AuthRepository.usuarioAtualId()

                            if (usuarioId != null) {

                                try {

                                    ContaSyncRepository.sincronizar(
                                        conta = contaAtualizada,
                                        usuarioId = usuarioId
                                    )

                                } catch (e: Exception) {

                                    Toast.makeText(
                                        context,
                                        "Alteração salva no aparelho, mas ainda não foi sincronizada.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }

                            resultado(true)
                        }
                    }
                },

                onDesativar = { conta ->

                    scope.launch {

                        contaDao.desativar(
                            conta.id
                        )

                        val usuarioId =
                            AuthRepository.usuarioAtualId()

                        if (usuarioId != null) {

                            try {

                                ContaSyncRepository.sincronizar(
                                    conta = conta.copy(
                                        ativa = false
                                    ),
                                    usuarioId = usuarioId
                                )

                            } catch (e: Exception) {

                                Toast.makeText(
                                    context,
                                    "Conta desativada localmente, mas ainda não foi sincronizada.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                },

                onReativar = { conta ->

                    scope.launch {

                        contaDao.reativar(
                            conta.id
                        )

                        val usuarioId =
                            AuthRepository.usuarioAtualId()

                        if (usuarioId != null) {

                            try {

                                ContaSyncRepository.sincronizar(
                                    conta = conta.copy(
                                        ativa = true
                                    ),
                                    usuarioId = usuarioId
                                )

                            } catch (e: Exception) {

                                Toast.makeText(
                                    context,
                                    "Conta reativada localmente, mas ainda não foi sincronizada.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                },

                onExcluir = { conta, resultado ->

                    scope.launch {

                        val quantidadeMovimentacoes =
                            dao.quantidadePorConta(
                                conta.id
                            )

                        val quantidadeCartoes =
                            cartoes.count { cartao ->
                                cartao.contaId == conta.id
                            }

                        val quantidadePagamentos =
                            pagamentosFatura.count { pagamento ->
                                pagamento.contaId == conta.id
                            }


                        when {

                            quantidadeMovimentacoes > 0 -> {

                                resultado(
                                    false,
                                    "Não é possível excluir a conta \"${conta.nome}\". " +
                                            "Existem $quantidadeMovimentacoes movimentação(ões) " +
                                            "vinculada(s) a ela. " +
                                            "Desative a conta para preservar o histórico."
                                )
                            }


                            quantidadeCartoes > 0 -> {

                                resultado(
                                    false,
                                    "Não é possível excluir a conta \"${conta.nome}\". " +
                                            "Existem $quantidadeCartoes cartão(ões) de crédito " +
                                            "vinculado(s) a ela. " +
                                            "Altere a conta vinculada ao cartão ou exclua o cartão primeiro."
                                )
                            }


                            quantidadePagamentos > 0 -> {

                                resultado(
                                    false,
                                    "Não é possível excluir a conta \"${conta.nome}\". " +
                                            "Existem $quantidadePagamentos pagamento(s) de fatura " +
                                            "registrado(s) nessa conta. " +
                                            "Desative a conta para preservar o histórico."
                                )
                            }


                            else -> {

                                try {

                                    val contaLocal =
                                        contaDao
                                            .listarTodasUmaVez()
                                            .firstOrNull { item ->
                                                item.id == conta.id
                                            }
                                            ?: throw IllegalStateException(
                                                "Conta local não encontrada."
                                            )


                                    val syncId =
                                        contaLocal.syncId
                                            ?: throw IllegalStateException(
                                                "A conta não possui syncId."
                                            )


                                    // Primeiro exclui no Supabase
                                    ContaSyncRepository.excluir(
                                        syncId = syncId
                                    )


                                    // Depois exclui no Room
                                    contaDao.excluir(
                                        conta.id
                                    )


                                    resultado(
                                        true,
                                        ""
                                    )


                                } catch (
                                    e: android.database.sqlite.SQLiteConstraintException
                                ) {

                                    resultado(
                                        false,
                                        "Não é possível excluir a conta \"${conta.nome}\" " +
                                                "porque ainda existem registros vinculados a ela. " +
                                                "Desative a conta para preservar o histórico."
                                    )


                                } catch (e: Exception) {

                                    resultado(
                                        false,
                                        "Não foi possível excluir a conta: ${e.message}"
                                    )
                                }
                            }
                        }
                    }
                },

                onVoltar = {
                    telaAtual = "inicio"
                }

            )
        }

        "configuracoes" -> {

            TelaConfiguracoes(
                modoTema =
                    modoTema,

                onModoTemaAlterado =
                    onModoTemaAlterado,

                planoAtual =
                    planoAtual,

                onVoltar = {
                    telaAtual =
                        "inicio"
                }
            )
        }

        "historico" -> {

            TelaHistorico(
                movimentacoes = movimentacoes,

                onEditar = { movimentacao ->

                    movimentacaoEmEdicao = movimentacao

                    telaAtual = "editar_movimentacao"
                },

                onExcluir = { movimentacao ->

                    scope.launch {

                        try {

                            val movimentacaoLocal =
                                dao
                                    .listarTodasUmaVez()
                                    .firstOrNull { item ->
                                        item.id == movimentacao.id
                                    }
                                    ?: throw IllegalStateException(
                                        "Movimentação local não encontrada."
                                    )


                            val syncId =
                                movimentacaoLocal.syncId
                                    ?: throw IllegalStateException(
                                        "A movimentação não possui syncId."
                                    )


                            // =============================================
                            // PRIMEIRO SUPABASE
                            // =============================================

                            MovimentacaoSyncRepository.excluir(
                                syncId = syncId
                            )


                            // =============================================
                            // DEPOIS ROOM
                            // FILHOS -> PAI
                            // =============================================

                            parcelaCartaoDao
                                .excluirPorMovimentacao(
                                    movimentacao.id
                                )

                            dao.excluir(
                                movimentacao.id
                            )


                            Toast.makeText(
                                context,
                                "Movimentação excluída.",
                                Toast.LENGTH_SHORT
                            ).show()


                        } catch (e: Exception) {

                            Toast.makeText(
                                context,
                                "Não foi possível excluir a movimentação: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                },

                onVoltar = {
                    telaAtual = "inicio"
                }
            )
        }
    }
    if (
        mostrarConfirmacaoRestauracao &&
        backupPendente != null
    ) {

        AlertDialog(
            onDismissRequest = {

                mostrarConfirmacaoRestauracao =
                    false

                backupPendente =
                    null
            },

            title = {

                Text(
                    text = "Restaurar backup?"
                )
            },

            text = {

                Column {

                    Text(
                        text =
                            "Os dados atuais do Blik serão substituídos pelos dados deste backup."
                    )

                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )

                    Text(
                        text =
                            "Esta ação não poderá ser desfeita.",
                        color =
                            MaterialTheme.colorScheme.error,
                        fontWeight =
                            FontWeight.SemiBold
                    )
                }
            },

            confirmButton = {

                Button(
                    onClick = {

                        val backup =
                            backupPendente
                                ?: return@Button

                        scope.launch {

                            try {

                                withContext(
                                    Dispatchers.IO
                                ) {

                                    banco.runInTransaction {

                                        val db =
                                            banco
                                                .openHelper
                                                .writableDatabase


                                        // =============================================
                                        // APAGA OS DADOS ATUAIS
                                        // FILHOS -> PAIS
                                        // =============================================

                                        db.execSQL(
                                            "DELETE FROM pagamentos_fatura"
                                        )

                                        db.execSQL(
                                            "DELETE FROM parcelas_cartao"
                                        )

                                        db.execSQL(
                                            "DELETE FROM movimentacoes"
                                        )

                                        db.execSQL(
                                            "DELETE FROM cartoes"
                                        )

                                        db.execSQL(
                                            "DELETE FROM categorias"
                                        )

                                        db.execSQL(
                                            "DELETE FROM contas"
                                        )


                                        // =============================================
                                        // RESTAURA CONTAS
                                        // =============================================

                                        val contasJson =
                                            backup.getJSONArray(
                                                "contas"
                                            )

                                        for (
                                        indice in
                                        0 until contasJson.length()
                                        ) {

                                            val item =
                                                contasJson.getJSONObject(
                                                    indice
                                                )

                                            db.execSQL(
                                                """
                            INSERT INTO contas (
                                id,
                                nome,
                                saldoInicial,
                                ativa
                            )
                            VALUES (?, ?, ?, ?)
                            """.trimIndent(),

                                                arrayOf(
                                                    item.getInt("id"),
                                                    item.getString("nome"),
                                                    item.getDouble(
                                                        "saldoInicial"
                                                    ),
                                                    if (
                                                        item.getBoolean(
                                                            "ativa"
                                                        )
                                                    ) {
                                                        1
                                                    } else {
                                                        0
                                                    }
                                                )
                                            )
                                        }


                                        // =============================================
                                        // RESTAURA CATEGORIAS
                                        // =============================================

                                        val categoriasJson =
                                            backup.getJSONArray(
                                                "categorias"
                                            )

                                        for (
                                        indice in
                                        0 until categoriasJson.length()
                                        ) {

                                            val item =
                                                categoriasJson
                                                    .getJSONObject(
                                                        indice
                                                    )

                                            db.execSQL(
                                                """
                            INSERT INTO categorias (
                                id,
                                nome
                            )
                            VALUES (?, ?)
                            """.trimIndent(),

                                                arrayOf(
                                                    item.getInt("id"),
                                                    item.getString("nome")
                                                )
                                            )
                                        }


                                        // =============================================
                                        // RESTAURA CARTÕES
                                        // =============================================

                                        val cartoesJson =
                                            backup.getJSONArray(
                                                "cartoes"
                                            )

                                        for (
                                        indice in
                                        0 until cartoesJson.length()
                                        ) {

                                            val item =
                                                cartoesJson.getJSONObject(
                                                    indice
                                                )

                                            db.execSQL(
                                                """
                            INSERT INTO cartoes (
                                id,
                                nome,
                                limite,
                                diaFechamento,
                                diaVencimento,
                                contaId
                            )
                            VALUES (?, ?, ?, ?, ?, ?)
                            """.trimIndent(),

                                                arrayOf(
                                                    item.getInt("id"),
                                                    item.getString("nome"),
                                                    item.getDouble("limite"),
                                                    item.getInt(
                                                        "diaFechamento"
                                                    ),
                                                    item.getInt(
                                                        "diaVencimento"
                                                    ),
                                                    item.getInt("contaId")
                                                )
                                            )
                                        }


                                        // =============================================
                                        // RESTAURA MOVIMENTAÇÕES
                                        // =============================================

                                        val movimentacoesJson =
                                            backup.getJSONArray(
                                                "movimentacoes"
                                            )

                                        for (
                                        indice in
                                        0 until movimentacoesJson.length()
                                        ) {

                                            val item =
                                                movimentacoesJson
                                                    .getJSONObject(
                                                        indice
                                                    )

                                            val formaPagamento =
                                                if (
                                                    item.isNull(
                                                        "formaPagamento"
                                                    )
                                                ) {
                                                    null
                                                } else {
                                                    item.getString(
                                                        "formaPagamento"
                                                    )
                                                }

                                            val contaId =
                                                if (
                                                    item.isNull(
                                                        "contaId"
                                                    )
                                                ) {
                                                    null
                                                } else {
                                                    item.getInt(
                                                        "contaId"
                                                    )
                                                }

                                            val contaDestinoId =
                                                if (
                                                    item.isNull(
                                                        "contaDestinoId"
                                                    )
                                                ) {
                                                    null
                                                } else {
                                                    item.getInt(
                                                        "contaDestinoId"
                                                    )
                                                }

                                            val categoriaId =
                                                if (
                                                    item.isNull(
                                                        "categoriaId"
                                                    )
                                                ) {
                                                    null
                                                } else {
                                                    item.getInt(
                                                        "categoriaId"
                                                    )
                                                }

                                            val cartaoId =
                                                if (
                                                    item.isNull(
                                                        "cartaoId"
                                                    )
                                                ) {
                                                    null
                                                } else {
                                                    item.getInt(
                                                        "cartaoId"
                                                    )
                                                }


                                            db.execSQL(
                                                """
                            INSERT INTO movimentacoes (
                                id,
                                descricao,
                                valor,
                                tipo,
                                formaPagamento,
                                contaId,
                                contaDestinoId,
                                categoriaId,
                                cartaoId,
                                quantidadeParcelas,
                                data
                            )
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """.trimIndent(),

                                                arrayOf<Any?>(
                                                    item.getInt("id"),
                                                    item.getString(
                                                        "descricao"
                                                    ),
                                                    item.getDouble(
                                                        "valor"
                                                    ),
                                                    item.getString(
                                                        "tipo"
                                                    ),
                                                    formaPagamento,
                                                    contaId,
                                                    contaDestinoId,
                                                    categoriaId,
                                                    cartaoId,
                                                    item.getInt(
                                                        "quantidadeParcelas"
                                                    ),
                                                    item.getString(
                                                        "data"
                                                    )
                                                )
                                            )
                                        }


                                        // =============================================
                                        // RESTAURA PARCELAS
                                        // =============================================

                                        val parcelasJson =
                                            backup.getJSONArray(
                                                "parcelas"
                                            )

                                        for (
                                        indice in
                                        0 until parcelasJson.length()
                                        ) {

                                            val item =
                                                parcelasJson.getJSONObject(
                                                    indice
                                                )

                                            db.execSQL(
                                                """
                            INSERT INTO parcelas_cartao (
                                id,
                                movimentacaoId,
                                cartaoId,
                                numeroParcela,
                                totalParcelas,
                                valor,
                                mesFatura,
                                anoFatura,
                                quitadaAnteriormente
                            )
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """.trimIndent(),

                                                arrayOf<Any?>(
                                                    item.getInt("id"),
                                                    item.getInt(
                                                        "movimentacaoId"
                                                    ),
                                                    item.getInt(
                                                        "cartaoId"
                                                    ),
                                                    item.getInt(
                                                        "numeroParcela"
                                                    ),
                                                    item.getInt(
                                                        "totalParcelas"
                                                    ),
                                                    item.getDouble(
                                                        "valor"
                                                    ),
                                                    item.getInt(
                                                        "mesFatura"
                                                    ),
                                                    item.getInt(
                                                        "anoFatura"
                                                    ),
                                                    if (
                                                        item.getBoolean(
                                                            "quitadaAnteriormente"
                                                        )
                                                    ) {
                                                        1
                                                    } else {
                                                        0
                                                    }
                                                )
                                            )
                                        }


                                        // =============================================
                                        // RESTAURA PAGAMENTOS DE FATURA
                                        // =============================================

                                        val pagamentosJson =
                                            backup.getJSONArray(
                                                "pagamentosFatura"
                                            )

                                        for (
                                        indice in
                                        0 until pagamentosJson.length()
                                        ) {

                                            val item =
                                                pagamentosJson
                                                    .getJSONObject(
                                                        indice
                                                    )

                                            db.execSQL(
                                                """
                            INSERT INTO pagamentos_fatura (
                                id,
                                cartaoId,
                                contaId,
                                mesFatura,
                                anoFatura,
                                valorPago,
                                dataPagamento
                            )
                            VALUES (?, ?, ?, ?, ?, ?, ?)
                            """.trimIndent(),

                                                arrayOf(
                                                    item.getInt("id"),
                                                    item.getInt(
                                                        "cartaoId"
                                                    ),
                                                    item.getInt(
                                                        "contaId"
                                                    ),
                                                    item.getInt(
                                                        "mesFatura"
                                                    ),
                                                    item.getInt(
                                                        "anoFatura"
                                                    ),
                                                    item.getDouble(
                                                        "valorPago"
                                                    ),
                                                    item.getString(
                                                        "dataPagamento"
                                                    )
                                                )
                                            )
                                        }
                                    }
                                }


                                // =============================================
                                // SUCESSO
                                // =============================================

                                mostrarConfirmacaoRestauracao =
                                    false

                                backupPendente =
                                    null

                                telaAtual =
                                    "inicio"

                                android.widget.Toast
                                    .makeText(
                                        context,
                                        "Backup restaurado com sucesso.",
                                        android.widget.Toast.LENGTH_LONG
                                    )
                                    .show()

                            } catch (e: Exception) {

                                android.widget.Toast
                                    .makeText(
                                        context,
                                        "Não foi possível restaurar o backup. Os dados atuais foram preservados.",
                                        android.widget.Toast.LENGTH_LONG
                                    )
                                    .show()
                            }
                        }
                    }
                ) {

                    Text(
                        text = "Restaurar"
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {

                        mostrarConfirmacaoRestauracao =
                            false

                        backupPendente =
                            null
                    }
                ) {

                    Text(
                        text = "Cancelar"
                    )
                }
            }
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaInicial(
    movimentacoes: List<Movimentacao>,
    contas: List<ContaEntity>,
    pagamentosFatura: List<PagamentoFaturaEntity>,
    parcelasCartao: List<ParcelaCartaoComDetalhes>,
    pagamentosFaturaComConta: List<PagamentoFaturaComConta>,
    cartoes: List<CartaoComConta>,
    onNovaMovimentacao: () -> Unit,
    onContas: () -> Unit,
    onCategorias: () -> Unit,
    onHistorico: () -> Unit,
    onCartoes: () -> Unit,
    onFaturas: () -> Unit,
    onAbrirFatura: (
            cartaoId: Int,
            mes: Int,
            ano: Int
            ) -> Unit,
    onExportarBackup: () -> Unit,
    onRestaurarBackup: () -> Unit,
    onConfiguracoes: () -> Unit,
    onSair: () -> Unit
) {
    val calendario = java.util.Calendar.getInstance()

    val mesAtual =
        String.format(
            "%02d",
            calendario.get(java.util.Calendar.MONTH) + 1
        )

    val anoAtual =
        calendario
            .get(java.util.Calendar.YEAR)
            .toString()

    val mesAtualInt = mesAtual.toInt()

    val anoAtualInt = anoAtual.toInt()

    val parcelasFaturaAtual =
        parcelasCartao.filter { parcela ->

            parcela.mesFatura == mesAtualInt &&
                    parcela.anoFatura == anoAtualInt &&
                    !parcela.quitadaAnteriormente
        }

    val faturasAtuais = parcelasCartao.filter { parcela ->
        !parcela.quitadaAnteriormente
    }
        .groupBy { parcela ->
            parcela.cartaoId
        }
        .mapNotNull { (cartaoId, parcelasDoCartao) ->
            val cartao =
                cartoes.find { item ->
                    item.id == cartaoId
                }
                    ?: return@mapNotNull null

            val faturasEmAberto = parcelasDoCartao
                .groupBy { parcela ->
                    parcela.mesFatura to
                            parcela.anoFatura
                }
                .mapNotNull { (periodo, parcelasDaFatura) ->
                    val mesFatura = periodo.first
                    val anoFatura = periodo.second
                    val total: Double = parcelasDaFatura
                        .sumOf { parcela: ParcelaCartaoComDetalhes ->
                            parcela.valor
                        }

                    val pago: Double = pagamentosFatura
                            .filter { pagamento: PagamentoFaturaEntity ->
                                pagamento.cartaoId == cartaoId &&
                                pagamento.mesFatura == mesFatura &&
                                pagamento.anoFatura == anoFatura
                            }
                        .sumOf { pagamento: PagamentoFaturaEntity ->
                            pagamento.valorPago
                        }

                    val restante: Double = (total - pago)
                        .coerceAtLeast(0.0)

                    if (restante < 0.1) {
                        null
                    } else {
                        Triple(
                            anoFatura,
                            mesFatura,
                            restante
                        )
                    }
                }
            val faturaSelecionada = faturasEmAberto
                .minWithOrNull(
                    compareBy<
                    Triple<Int, Int, Double>
                    > { fatura ->
                        fatura.first
                    }
                        .thenBy { fatura ->
                            fatura.second
                        }
                )
                ?: return@mapNotNull null

            val anoFatura = faturaSelecionada.first
            val mesFatura = faturaSelecionada.second
            val restante = faturaSelecionada.third
            val fechamento = criarDataFatura(
                dia = cartao.diaFechamento,
                mes= mesFatura,
                ano = anoFatura
            )

            val vencimento = calcularVencimentoFatura(
                mesFatura = mesFatura,
                anoFatura = anoFatura,
                diaFechamento = cartao.diaFechamento,
                diaVencimento = cartao.diaVencimento
            )

            val status = calcularStatusFatura(
                restante = restante,
                mesFatura = mesFatura,
                anoFatura = anoFatura,
                diaFechamento = cartao.diaFechamento,
                diaVencimento = cartao.diaVencimento
            )

            val limiteUtilizado = calcularLimiteUtilizadoCartao(
                cartaoId = cartao.id,
                parcelasCartao = parcelasCartao,
                pagamentosFatura = pagamentosFatura
            )
            ResumoFatura(
                cartaoId = cartao.id,
                cartaoNome=cartao.nome,
                mesFatura = mesFatura,
                anoFatura = anoFatura,
                restante = restante,
                fechamento = fechamento,
                vencimento = vencimento,
                status = status,
                limite = cartao.limite,
                limiteUtilizado = limiteUtilizado
            )
                    }
        .sortedBy {
            it.vencimento.timeInMillis
                }

    val nomesMeses = listOf(
        "Janeiro",
        "Fevereiro",
        "Março",
        "Abril",
        "Maio",
        "Junho",
        "Julho",
        "Agosto",
        "Setembro",
        "Outubro",
        "Novembro",
        "Dezembro"
    )

    val nomeMesAtual =
        nomesMeses[
            mesAtual.toInt() - 1
        ]

    val periodoAtual =
        "$nomeMesAtual"

    val movimentacoesDoMes =
        movimentacoes.filter { movimentacao ->

            val partes =
                movimentacao.data.split("/")

            if (partes.size != 3) {

                false

            } else {

                val mesMovimentacao =
                    partes[1]
                        .toIntOrNull()

                val anoMovimentacao =
                    partes[2]
                        .toIntOrNull()


                mesMovimentacao ==
                        mesAtualInt &&

                        anoMovimentacao ==
                        anoAtualInt
            }
        }

    val entradasDoMes =
        movimentacoesDoMes
            .filter { movimentacao ->
                movimentacao.tipo == "Entrada"
            }
            .sumOf { movimentacao ->
                movimentacao.valor
            }

    val saidasDoMes =
        movimentacoesDoMes
            .filter { movimentacao ->

                movimentacao.tipo == "Saída"
            }
            .sumOf { movimentacao ->

                movimentacao.valor
            }

    val saldosPorConta: Map<ContaEntity, Double> =
        contas.associateWith { conta: ContaEntity ->

            val entradas: Double =
                movimentacoes
                    .filter { movimentacao ->
                        movimentacao.tipo == "Entrada" &&
                                movimentacao.contaId == conta.id
                    }
                    .sumOf { movimentacao ->
                        movimentacao.valor
                    }

            val saidasDaConta: Double =
                movimentacoes
                    .filter { movimentacao ->
                        movimentacao.tipo == "Saída" &&
                                movimentacao.formaPagamento == "Conta" &&
                                movimentacao.contaId == conta.id
                    }
                    .sumOf { movimentacao ->
                        movimentacao.valor
                    }

            val transferenciasSaindo: Double =
                movimentacoes
                    .filter { movimentacao ->
                        movimentacao.tipo == "Transferência" &&
                                movimentacao.contaId == conta.id
                    }
                    .sumOf { movimentacao ->
                        movimentacao.valor
                    }

            val transferenciasEntrando: Double =
                movimentacoes
                    .filter { movimentacao ->
                        movimentacao.tipo == "Transferência" &&
                                movimentacao.contaDestinoId == conta.id
                    }
                    .sumOf { movimentacao ->
                        movimentacao.valor
                    }

            val pagamentosDeFatura: Double =
                pagamentosFatura
                    .filter { pagamento ->
                        pagamento.contaId == conta.id
                    }
                    .sumOf { pagamento ->
                        pagamento.valorPago
                    }

            conta.saldoInicial +
                    entradas -
                    saidasDaConta -
                    transferenciasSaindo +
                    transferenciasEntrando -
                    pagamentosDeFatura
        }

    val saldoAtual: Double =
        saldosPorConta.values.sum()

    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )

    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(

        drawerState = drawerState,

        drawerContent = {

            ModalDrawerSheet(
                drawerContainerColor =
                    MaterialTheme.colorScheme.surface
            ) {

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                Column(
                    modifier =
                        Modifier.padding(
                            horizontal = 20.dp
                        )
                ) {

                    MarcaBlik()

                }

                val coresItemDrawer =
                    androidx.compose.material3.NavigationDrawerItemDefaults.colors(
                        selectedContainerColor =
                            MaterialTheme.colorScheme.primaryContainer,

                        selectedTextColor =
                            MaterialTheme.colorScheme.primary,

                        unselectedContainerColor =
                            androidx.compose.ui.graphics.Color.Transparent,

                        unselectedTextColor =
                            MaterialTheme.colorScheme.onSurface
                    )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                NavigationDrawerItem(
                    label = {
                        Text(
                            text = "Início",
                            fontWeight = FontWeight.SemiBold
                        )
                    },

                    selected = true,

                    onClick = {
                        scope.launch {
                            drawerState.close()
                        }
                    },

                    modifier =
                        Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 2.dp
                        ),

                    shape =
                        androidx.compose.foundation.shape.RoundedCornerShape(
                            14.dp
                        ),

                    colors = coresItemDrawer
                )

                NavigationDrawerItem(
                    label = {
                        Text("Histórico")
                    },

                    selected = false,

                    onClick = {

                        scope.launch {
                            drawerState.close()
                        }

                        onHistorico()
                    },

                    modifier =
                        Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 2.dp
                        ),

                    shape =
                        androidx.compose.foundation.shape.RoundedCornerShape(
                            14.dp
                        ),

                    colors = coresItemDrawer
                )

                NavigationDrawerItem(
                    label = {
                        Text("Contas")
                    },

                    selected = false,

                    onClick = {

                        scope.launch {
                            drawerState.close()
                        }

                        onContas()
                    },

                    modifier =
                        Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 2.dp
                        ),

                    shape =
                        androidx.compose.foundation.shape.RoundedCornerShape(
                            14.dp
                        ),

                    colors = coresItemDrawer
                )

                NavigationDrawerItem(
                    label = {
                        Text("Cartões")
                    },

                    selected = false,

                    onClick = {

                        scope.launch {
                            drawerState.close()
                        }

                        onCartoes()
                    },

                    modifier =
                        Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 2.dp
                        ),

                    shape =
                        androidx.compose.foundation.shape.RoundedCornerShape(
                            14.dp
                        ),

                    colors = coresItemDrawer
                )

                NavigationDrawerItem(
                    label = {
                        Text("Faturas")
                    },

                    selected = false,

                    onClick = {

                        scope.launch {
                            drawerState.close()
                        }

                        onFaturas()
                    },

                    modifier =
                        Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 2.dp
                        ),

                    shape =
                        androidx.compose.foundation.shape.RoundedCornerShape(
                            14.dp
                        ),

                    colors = coresItemDrawer
                )

                NavigationDrawerItem(
                    label = {
                        Text("Categorias")
                    },

                    selected = false,

                    onClick = {

                        scope.launch {
                            drawerState.close()
                        }

                        onCategorias()
                    },

                    modifier =
                        Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 2.dp
                        ),

                    shape =
                        androidx.compose.foundation.shape.RoundedCornerShape(
                            14.dp
                        ),

                    colors = coresItemDrawer
                )

                NavigationDrawerItem(
                    label = {
                        Text(
                            "Configurações"
                        )
                    },

                    selected = false,

                    onClick = {

                        scope.launch {
                            drawerState.close()
                        }

                        onConfiguracoes()
                    },

                    modifier =
                        Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 2.dp
                        ),

                    shape =
                        androidx.compose.foundation.shape
                            .RoundedCornerShape(
                                14.dp
                            ),

                    colors =
                        coresItemDrawer
                )

                NavigationDrawerItem(
                    label = {
                        Text("Exportar backup")
                    },

                    selected = false,

                    onClick = {

                        scope.launch {
                            drawerState.close()
                        }

                        onExportarBackup()
                    },

                    modifier =
                        Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 2.dp
                        ),

                    shape =
                        androidx.compose.foundation.shape.RoundedCornerShape(
                            14.dp
                        ),

                    colors = coresItemDrawer
                )

                NavigationDrawerItem(
                    label = {
                        Text("Restaurar backup")
                    },

                    selected = false,

                    onClick = {

                        scope.launch {
                            drawerState.close()
                        }

                        onRestaurarBackup()
                    },

                    modifier =
                        Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 2.dp
                        ),

                    shape =
                        androidx.compose.foundation.shape.RoundedCornerShape(
                            14.dp
                        ),

                    colors = coresItemDrawer
                )

                NavigationDrawerItem(
                    label = {
                        Text(
                            text = "Sair da conta",
                            color = MaterialTheme.colorScheme.error
                        )
                    },

                    selected = false,

                    onClick = {

                        scope.launch {
                            drawerState.close()
                        }

                        onSair()
                    }
                )
            }
        }

    ) {

        Scaffold(

            topBar = {

                TopAppBar(

                    title = {
                        MarcaBlik()
                    },

                    navigationIcon = {

                        IconButton(
                            onClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }
                        ) {

                            Text(
                                text = "☰",
                                fontSize = 25.sp,
                                fontWeight = FontWeight.Medium,
                                color =
                                    MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },

                    colors =
                        androidx.compose.material3.TopAppBarDefaults
                            .topAppBarColors(
                                containerColor =
                                    MaterialTheme.colorScheme.background,

                                titleContentColor =
                                    MaterialTheme.colorScheme.onBackground,

                                navigationIconContentColor =
                                    MaterialTheme.colorScheme.onBackground
                            )
                )
            },

            floatingActionButton = {

                androidx.compose.material3.FloatingActionButton(
                    onClick = onNovaMovimentacao,

                    containerColor =
                        MaterialTheme.colorScheme.primary,

                    contentColor =
                        MaterialTheme.colorScheme.onPrimary,

                    shape =
                        androidx.compose.foundation.shape.CircleShape
                ) {

                    Text(
                        text = "+",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            },

            containerColor =
                MaterialTheme.colorScheme.background

        ) { innerPadding ->

            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),

                contentPadding = androidx.compose.foundation.layout.PaddingValues (
                    start = 20.dp,
                    end = 20.dp,
                    top = 12.dp,
                    bottom = 100.dp
                )
            ) {
                item {
                    val usarTemaEscuro =
                        MaterialTheme.colorScheme.background
                            .luminance() < 0.5f


                    val corTextoCard =
                        if (usarTemaEscuro) {
                            Color(0xFFF5F7FA)
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        }


                    val corTextoSecundarioCard =
                        if (usarTemaEscuro) {
                            Color(0xFFF5F7FA)
                                .copy(alpha = 0.72f)
                        } else {
                            MaterialTheme.colorScheme
                                .onPrimaryContainer
                                .copy(alpha = 0.75f)
                        }


// =====================================================
// CARD PRINCIPAL - SALDO
// =====================================================

                    Card(
                        modifier =
                            Modifier.fillMaxWidth(),

                        shape =
                            RoundedCornerShape(24.dp),

                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    if (usarTemaEscuro) {
                                        Color.Transparent
                                    } else {
                                        // mantém o verde original no tema claro
                                        MaterialTheme.colorScheme.primaryContainer
                                    }
                            ),

                        elevation =
                            CardDefaults.cardElevation(
                                defaultElevation =
                                    if (usarTemaEscuro) {
                                        3.dp
                                    } else {
                                        0.dp
                                    }
                            )
                    ) {

                        Box(
                            modifier =
                                if (usarTemaEscuro) {

                                    Modifier
                                        .fillMaxWidth()

                                        // fundo grafite refinado
                                        .background(
                                            brush =
                                                Brush.linearGradient(
                                                    colors =
                                                        listOf(
                                                            Color(0xFF2B2F34),
                                                            Color(0xFF3A3F45),
                                                            Color(0xFF30343A)
                                                        ),

                                                    start =
                                                        Offset.Zero,

                                                    end =
                                                        Offset(
                                                            900f,
                                                            700f
                                                        )
                                                ),

                                            shape =
                                                RoundedCornerShape(
                                                    24.dp
                                                )
                                        )

                                        // contorno discreto
                                        .border(
                                            width =
                                                1.dp,

                                            color =
                                                Color.White.copy(
                                                    alpha = 0.08f
                                                ),

                                            shape =
                                                RoundedCornerShape(
                                                    24.dp
                                                )
                                        )

                                        .padding(
                                            horizontal = 20.dp,
                                            vertical = 20.dp
                                        )

                                } else {

                                    // no claro não aplicamos gradiente,
                                    // brilho ou outro efeito.
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = 20.dp,
                                            vertical = 20.dp
                                        )
                                }
                        ) {

                            Column(
                                modifier =
                                    Modifier.fillMaxWidth()
                            ) {

                                // =========================================
                                // SALDO TOTAL
                                // =========================================

                                Text(
                                    text =
                                        "Saldo total",

                                    fontSize =
                                        16.sp,

                                    color =
                                        corTextoSecundarioCard
                                )


                                Spacer(
                                    modifier =
                                        Modifier.height(6.dp)
                                )


                                Text(
                                    text =
                                        formatarDinheiro(
                                            saldoAtual
                                        ),

                                    fontSize =
                                        32.sp,

                                    fontWeight =
                                        FontWeight.Bold,

                                    color =
                                        corTextoCard,

                                    maxLines =
                                        1,

                                    softWrap =
                                        false
                                )


                                Spacer(
                                    modifier =
                                        Modifier.height(22.dp)
                                )


                                // =========================================
                                // ENTRADAS / PERÍODO / SAÍDAS
                                // =========================================

                                Row(
                                    modifier =
                                        Modifier.fillMaxWidth(),

                                    verticalAlignment =
                                        Alignment.Top
                                ) {

                                    // =====================================
                                    // ENTRADAS
                                    // =====================================

                                    Column(
                                        modifier =
                                            Modifier.weight(1f),

                                        horizontalAlignment =
                                            Alignment.Start
                                    ) {

                                        Text(
                                            text =
                                                "Entradas",

                                            fontSize =
                                                13.sp,

                                            color =
                                                corTextoSecundarioCard,

                                            maxLines =
                                                1
                                        )


                                        Spacer(
                                            modifier =
                                                Modifier.height(4.dp)
                                        )


                                        Text(
                                            text =
                                                formatarDinheiro(
                                                    entradasDoMes
                                                ),

                                            fontSize =
                                                16.sp,

                                            fontWeight =
                                                FontWeight.SemiBold,

                                            color =
                                                corTextoCard,

                                            maxLines =
                                                1,

                                            softWrap =
                                                false
                                        )
                                    }


                                    // =====================================
                                    // PERÍODO
                                    // =====================================

                                    Column(
                                        modifier =
                                            Modifier.weight(1.15f),

                                        horizontalAlignment =
                                            Alignment.CenterHorizontally
                                    ) {

                                        Text(
                                            text =
                                                periodoAtual,

                                            fontSize =
                                                12.sp,

                                            color =
                                                corTextoSecundarioCard,

                                            textAlign =
                                                TextAlign.Center,

                                            maxLines =
                                                2
                                        )


                                        Spacer(
                                            modifier =
                                                Modifier.height(4.dp)
                                        )


                                        Text(
                                            text =
                                                "—",

                                            fontSize =
                                                16.sp,

                                            color =
                                                corTextoCard.copy(
                                                    alpha = 0.40f
                                                )
                                        )
                                    }


                                    // =====================================
                                    // SAÍDAS
                                    // =====================================

                                    Column(
                                        modifier =
                                            Modifier.weight(1f),

                                        horizontalAlignment =
                                            Alignment.End
                                    ) {

                                        Text(
                                            text =
                                                "Saídas",

                                            fontSize =
                                                13.sp,

                                            color =
                                                corTextoSecundarioCard,

                                            textAlign =
                                                TextAlign.End,

                                            maxLines =
                                                1
                                        )


                                        Spacer(
                                            modifier =
                                                Modifier.height(4.dp)
                                        )


                                        Text(
                                            text =
                                                formatarDinheiro(
                                                    saidasDoMes
                                                ),

                                            fontSize =
                                                16.sp,

                                            fontWeight =
                                                FontWeight.SemiBold,

                                            color =
                                                corTextoCard,

                                            textAlign =
                                                TextAlign.End,

                                            maxLines =
                                                1,

                                            softWrap =
                                                false
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(
                        modifier = Modifier.height(24.dp)
                    )

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),

                        horizontalArrangement =
                            Arrangement.SpaceBetween,

                        verticalAlignment =
                            androidx.compose.ui.Alignment.CenterVertically
                    ) {

                        Text(
                            text = "Contas",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color =
                                MaterialTheme.colorScheme.onBackground
                        )

                        Text(
                            text = "Ver todas",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color =
                                MaterialTheme.colorScheme.primary,

                            modifier =
                                Modifier.clickable {
                                    onContas()
                                }
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )
                }

                if (contas.isEmpty()) {
                    item {
                        Text(
                            text = "Nenhuma conta cadastrada."
                        )
                    }
                } else {
                    items(
                        items = contas,
                        key = { conta ->
                            conta.id
                        }
                    ) { conta ->
                        val saldo = saldosPorConta[conta] ?: 0.0

                        ContaItem(
                            nome = conta.nome,
                            saldo =
                                formatarDinheiro(
                                    saldo
                                ),
                            onClick = {
                                onContas()
                            }
                        )
                    }
                }

                item {

                    Spacer(
                        modifier = Modifier.height(24.dp)
                    )

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),

                        horizontalArrangement =
                            Arrangement.SpaceBetween,

                        verticalAlignment =
                            androidx.compose.ui.Alignment.CenterVertically
                    ) {

                        Text(
                            text = "Faturas do mês",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color =
                                MaterialTheme.colorScheme.onBackground
                        )

                        Text(
                            text = "Ver todas",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color =
                                MaterialTheme.colorScheme.primary,

                            modifier =
                                Modifier.clickable {
                                    onFaturas()
                                }
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )
                }

                if (faturasAtuais.isEmpty()) {

                    item {

                        Card(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onFaturas()
                                    },

                            shape =
                                androidx.compose.foundation.shape
                                    .RoundedCornerShape(18.dp),

                            colors =
                                androidx.compose.material3.CardDefaults
                                    .cardColors(
                                        containerColor =
                                            MaterialTheme.colorScheme.surface
                                    ),

                            elevation =
                                androidx.compose.material3.CardDefaults
                                    .cardElevation(
                                        defaultElevation = 1.dp
                                    )
                        ) {

                            Column(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = 18.dp,
                                            vertical = 20.dp
                                        ),

                                horizontalAlignment =
                                    androidx.compose.ui.Alignment.CenterHorizontally
                            ) {

                                Text(
                                    text = "Nenhuma fatura neste mês",

                                    fontSize = 16.sp,

                                    fontWeight =
                                        FontWeight.SemiBold,

                                    textAlign =
                                        TextAlign.Center,

                                    color =
                                        MaterialTheme.colorScheme.onSurface
                                )


                                Spacer(
                                    modifier =
                                        Modifier.height(5.dp)
                                )


                                Text(
                                    text =
                                        "Não há compras no crédito " +
                                                "para o período atual.",

                                    fontSize = 13.sp,

                                    textAlign =
                                        TextAlign.Center,

                                    color =
                                        MaterialTheme.colorScheme
                                            .onSurfaceVariant
                                )


                                Spacer(
                                    modifier =
                                        Modifier.height(12.dp)
                                )


                                Text(
                                    text = "Ver faturas",

                                    fontSize = 13.sp,

                                    fontWeight =
                                        FontWeight.SemiBold,

                                    color =
                                        MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }else {
                    items(
                        items = faturasAtuais
                    ) { fatura ->
                        val corStatus =
                            when (fatura.status) {

                                "Vencida" ->
                                    MaterialTheme.colorScheme.error

                                "Fechada" ->
                                    MaterialTheme.colorScheme.tertiary

                                else ->
                                    MaterialTheme.colorScheme.primary
                            }

                        val corFundoStatus =
                            when (fatura.status) {

                                "Vencida" ->
                                    MaterialTheme.colorScheme.errorContainer

                                "Fechada" ->
                                    MaterialTheme.colorScheme.tertiaryContainer

                                else ->
                                    MaterialTheme.colorScheme.primaryContainer
                            }


                        Card(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                                    .clickable {
                                        onAbrirFatura(
                                            fatura.cartaoId,
                                            fatura.mesFatura,
                                            fatura.anoFatura
                                        )
                                    },

                            shape =
                                androidx.compose.foundation.shape.RoundedCornerShape(
                                    18.dp
                                ),

                            colors =
                                androidx.compose.material3.CardDefaults.cardColors(
                                    containerColor =
                                        MaterialTheme.colorScheme.surface
                                ),

                            elevation =
                                androidx.compose.material3.CardDefaults.cardElevation(
                                    defaultElevation = 2.dp
                                )
                        ) {

                            Column(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                            ) {

                                // =============================================
                                // CARTÃO + STATUS
                                // =============================================

                                Row(
                                    modifier =
                                        Modifier.fillMaxWidth(),

                                    horizontalArrangement =
                                        Arrangement.SpaceBetween,

                                    verticalAlignment =
                                        androidx.compose.ui.Alignment.CenterVertically
                                ) {

                                    Text(
                                        text = fatura.cartaoNome,
                                        fontWeight =
                                            FontWeight.SemiBold,
                                        fontSize = 17.sp,
                                        color =
                                            MaterialTheme.colorScheme.onSurface
                                    )


                                    Box(
                                        modifier =
                                            Modifier.background(
                                                color =
                                                    corFundoStatus,

                                                shape =
                                                    androidx.compose.foundation.shape
                                                        .RoundedCornerShape(
                                                            50.dp
                                                        )
                                            )
                                    ) {

                                        Text(
                                            text =
                                                fatura.status.uppercase(),

                                            modifier =
                                                Modifier.padding(
                                                    horizontal = 10.dp,
                                                    vertical = 4.dp
                                                ),

                                            fontSize = 11.sp,
                                            fontWeight =
                                                FontWeight.Bold,

                                            color =
                                                corStatus
                                        )
                                    }
                                }


                                Spacer(
                                    modifier =
                                        Modifier.height(14.dp)
                                )


                                val mostrarFechamento =
                                    fatura.status == "Aberta"

                                val tituloData =
                                    if (mostrarFechamento) {
                                        "Fechamento"
                                    } else {
                                        "Vencimento"
                                    }

                                val dataExibida =
                                    if (mostrarFechamento) {
                                        fatura.fechamento
                                    } else {
                                        fatura.vencimento
                                    }


                                // =============================================
                                // DATA + VALOR
                                // =============================================

                                Row(
                                    modifier =
                                        Modifier.fillMaxWidth(),

                                    horizontalArrangement =
                                        Arrangement.SpaceBetween,

                                    verticalAlignment =
                                        androidx.compose.ui.Alignment.Bottom
                                ) {

                                    Column {

                                        Text(
                                            text = tituloData,
                                            fontSize = 12.sp,
                                            color =
                                                MaterialTheme.colorScheme
                                                    .onSurfaceVariant
                                        )

                                        Spacer(
                                            modifier =
                                                Modifier.height(2.dp)
                                        )

                                        Text(
                                            text =
                                                formatarDataCalendario(
                                                    dataExibida
                                                ),

                                            fontSize = 14.sp,
                                            fontWeight =
                                                FontWeight.Medium
                                        )
                                    }


                                    Column(
                                        horizontalAlignment =
                                            androidx.compose.ui.Alignment.End
                                    ) {

                                        Text(
                                            text = "Em aberto",
                                            fontSize = 12.sp,
                                            color =
                                                MaterialTheme.colorScheme
                                                    .onSurfaceVariant
                                        )

                                        Spacer(
                                            modifier =
                                                Modifier.height(2.dp)
                                        )

                                        Text(
                                            text =
                                                formatarDinheiro(
                                                    fatura.restante
                                                ),

                                            fontSize = 17.sp,
                                            fontWeight =
                                                FontWeight.Bold,

                                            color =
                                                corStatus
                                        )
                                    }
                                }
                                Spacer(
                                    modifier = Modifier.height(16.dp)
                                )


                                val percentualUso =
                                    if (fatura.limite > 0.0) {

                                        fatura.limiteUtilizado /
                                                fatura.limite

                                    } else {
                                        0.0
                                    }


                                val progressoBarra =
                                    percentualUso
                                        .coerceIn(
                                            0.0,
                                            1.0
                                        )
                                        .toFloat()


                                val percentualExibido =
                                    (percentualUso * 100)
                                        .toInt()


                                val limiteDisponivel =
                                    (
                                            fatura.limite -
                                                    fatura.limiteUtilizado
                                            )
                                        .coerceAtLeast(0.0)


                                val corBarra =
                                    when {

                                        percentualUso >= 0.90 ->
                                            MaterialTheme.colorScheme.error

                                        percentualUso >= 0.70 ->
                                            MaterialTheme.colorScheme.tertiary

                                        else ->
                                            MaterialTheme.colorScheme.primary
                                    }


// =============================================
// TÍTULO + PERCENTUAL
// =============================================

                                Row(
                                    modifier =
                                        Modifier.fillMaxWidth(),

                                    horizontalArrangement =
                                        Arrangement.SpaceBetween,

                                    verticalAlignment =
                                        androidx.compose.ui.Alignment.CenterVertically
                                ) {

                                    Text(
                                        text = "Limite utilizado",
                                        fontSize = 12.sp,
                                        color =
                                            MaterialTheme.colorScheme
                                                .onSurfaceVariant
                                    )


                                    Text(
                                        text = "$percentualExibido%",
                                        fontSize = 12.sp,
                                        fontWeight =
                                            FontWeight.SemiBold,
                                        color = corBarra
                                    )
                                }


                                Spacer(
                                    modifier = Modifier.height(6.dp)
                                )


// =============================================
// BARRA
// =============================================

                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .background(
                                                color =
                                                    MaterialTheme.colorScheme
                                                        .surfaceVariant,
                                                shape =
                                                    androidx.compose.foundation.shape
                                                        .RoundedCornerShape(50.dp)
                                            )
                                ) {

                                    if (progressoBarra > 0f) {

                                        Box(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth(
                                                        progressoBarra
                                                    )
                                                    .height(8.dp)
                                                    .background(
                                                        color = corBarra,
                                                        shape =
                                                            androidx.compose.foundation.shape
                                                                .RoundedCornerShape(50.dp)
                                                    )
                                        )
                                    }
                                }


                                Spacer(
                                    modifier = Modifier.height(10.dp)
                                )


// =============================================
// UTILIZADO + DISPONÍVEL
// =============================================

                                Row(
                                    modifier =
                                        Modifier.fillMaxWidth(),

                                    horizontalArrangement =
                                        Arrangement.SpaceBetween,

                                    verticalAlignment =
                                        androidx.compose.ui.Alignment.Top
                                ) {

                                   }
                            }
                        }
                    }
                    item {
                        Spacer(
                            modifier = Modifier.height(20.dp)
                        )
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaNovaMovimentacao(
    contas: List<ContaEntity>,
    categorias: List<CategoriaEntity>,
    cartoes: List<CartaoComConta>,
    movimentacaoParaEditar: Movimentacao? = null,
    onSalvar: (Movimentacao) -> Unit,
    onVoltar: () -> Unit
){
    BackHandler {
        onVoltar()
    }

    var descricao by remember {
        mutableStateOf(
            movimentacaoParaEditar?.descricao ?: ""
        )
    }

    var valor by remember {
        mutableStateOf(
            movimentacaoParaEditar
                ?.valor
                ?.toString()
                ?.replace(".", ",")
                ?: ""
        )
    }

    var tipo by remember {
        mutableStateOf(
            movimentacaoParaEditar?.tipo ?: ""
        )
    }

    var formaPagamento by remember {
        mutableStateOf(
            movimentacaoParaEditar?.formaPagamento ?: ""
        )
    }

    var contaSelecionada by remember {
        mutableStateOf<ContaEntity?>(null)
    }

    var contaDestinoSelecionada by remember {
        mutableStateOf<ContaEntity?>(null)
    }

    var categoriaSelecionada by remember {
        mutableStateOf<CategoriaEntity?>(null)
    }

    var cartaoSelecionado by remember {
        mutableStateOf<CartaoComConta?>(null)
    }

    var quantidadeParcelas by remember {
        mutableStateOf(
            movimentacaoParaEditar
                ?.quantidadeParcelas
                ?.toString()
                ?: "1"
        )
    }


    var data by remember {
        mutableStateOf(
            movimentacaoParaEditar?.data
                ?: SimpleDateFormat(
                    "dd/MM/yyyy",
                    Locale.getDefault()
                ).format(Date())
        )
    }

    var mensagem by remember {
        mutableStateOf("")
    }

    // POPUPS

    var mostrarCalendario by remember {
        mutableStateOf(false)
    }


    // CARREGA DADOS QUANDO FOR EDIÇÃO

    LaunchedEffect(
        contas,
        categorias,
        cartoes,
        movimentacaoParaEditar
    ) {

        movimentacaoParaEditar?.let { movimentacao ->

            contaSelecionada =
                contas.find {
                    it.id == movimentacao.contaId
                }

            contaDestinoSelecionada =
                contas.find {
                    it.id == movimentacao.contaDestinoId
                }

            categoriaSelecionada =
                categorias.find {
                    it.id == movimentacao.categoriaId
                }

            cartaoSelecionado =
                cartoes.find {
                    it.id == movimentacao.cartaoId
                }
        }
    }


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor =
            MaterialTheme.colorScheme.background
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(20.dp)
                .fillMaxSize()
        ) {

            item {

                Text(
                    text =
                        if (movimentacaoParaEditar == null) {
                            "Nova movimentação"
                        } else {
                            "Editar movimentação"
                        },

                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color =
                        MaterialTheme.colorScheme.onBackground
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text =
                        if (movimentacaoParaEditar == null) {
                            "Registre uma nova entrada, saída ou transferência"
                        } else {
                            "Atualize os dados da movimentação"
                        },

                    fontSize = 14.sp,
                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                OutlinedTextField(
                    value = descricao,

                    onValueChange = {
                        descricao = it
                        mensagem = ""
                    },

                    label = {
                        Text("Descrição")
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    singleLine = true,

                    shape =
                        androidx.compose.foundation.shape
                            .RoundedCornerShape(14.dp)
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                // VALOR

                OutlinedTextField(
                    value = valor,

                    onValueChange = {
                        valor = it
                        mensagem = ""
                    },

                    label = {
                        Text("Valor")
                    },

                    placeholder = {
                        Text("Ex.: 150,00")
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    singleLine = true,

                    shape =
                        androidx.compose.foundation.shape
                            .RoundedCornerShape(14.dp)
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )


                // TIPO

                Text(
                    text = "Tipo",

                    fontSize = 13.sp,

                    fontWeight =
                        FontWeight.Medium,

                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )


                DropdownBlik(
                    valorSelecionado =
                        if (tipo.isBlank()) {
                            "Selecione"
                        } else {
                            tipo
                        },

                    opcoes =
                        listOf(
                            "Entrada",
                            "Saída",
                            "Transferência"
                        ),

                    modifier =
                        Modifier.fillMaxWidth(),

                    onSelecionar = { novoTipo ->

                        when (novoTipo) {

                            "Entrada" -> {

                                tipo = "Entrada"

                                formaPagamento = ""

                                cartaoSelecionado = null

                                contaDestinoSelecionada = null

                                quantidadeParcelas = "1"
                            }


                            "Saída" -> {

                                tipo = "Saída"

                                contaDestinoSelecionada = null
                            }


                            "Transferência" -> {

                                tipo = "Transferência"

                                formaPagamento = ""

                                categoriaSelecionada = null

                                cartaoSelecionado = null

                                quantidadeParcelas = "1"
                            }
                        }

                        mensagem = ""
                    }
                )


                // -------------------------
                // ENTRADA
                // -------------------------

                if (tipo == "Entrada") {

                    Spacer(
                        modifier = Modifier.height(14.dp)
                    )

                    Text(
                        text = "Conta",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    DropdownBlik(
                        valorSelecionado =
                            contaSelecionada?.nome
                                ?: "Selecione",

                        opcoes =
                            contas.map { conta ->
                                conta.nome
                            },

                        modifier =
                            Modifier.fillMaxWidth(),

                        onSelecionar = { nomeConta ->

                            contaSelecionada =
                                contas.firstOrNull { conta ->
                                    conta.nome == nomeConta
                                }

                            mensagem = ""
                        }
                    )

                    Spacer(
                        modifier = Modifier.height(14.dp)
                    )

                    Text(
                        text = "Categoria",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    DropdownBlik(
                        valorSelecionado =
                            categoriaSelecionada?.nome
                                ?: "Selecione",

                        opcoes =
                            categorias.map { categoria ->
                                categoria.nome
                            },

                        modifier =
                            Modifier.fillMaxWidth(),

                        onSelecionar = { nomeCategoria ->

                            categoriaSelecionada =
                                categorias.firstOrNull { categoria ->
                                    categoria.nome == nomeCategoria
                                }

                            mensagem = ""
                        }
                    )
                }


                // -------------------------
                // SAÍDA
                // -------------------------

                if (tipo == "Saída") {

                    Spacer(
                        modifier = Modifier.height(14.dp)
                    )

                    Text(
                        text = "Forma de pagamento",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    DropdownBlik(
                        valorSelecionado =
                            when (formaPagamento) {
                                "Conta" -> "Conta / Débito"
                                "Crédito" -> "Crédito"
                                else -> "Selecione"
                            },

                        opcoes =
                            listOf(
                                "Conta / Débito",
                                "Crédito"
                            ),

                        modifier =
                            Modifier.fillMaxWidth(),

                        onSelecionar = { opcao ->

                            when (opcao) {

                                "Conta / Débito" -> {

                                    formaPagamento = "Conta"

                                    cartaoSelecionado = null

                                    quantidadeParcelas = "1"
                                }

                                "Crédito" -> {

                                    formaPagamento = "Crédito"

                                    contaSelecionada = null
                                }
                            }

                            mensagem = ""
                        }
                    )


                    // SAÍDA PELA CONTA

                    if (formaPagamento == "Conta") {

                        Spacer(
                            modifier = Modifier.height(14.dp)
                        )

                        Text(
                            text = "Conta",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        DropdownBlik(
                            valorSelecionado =
                                contaSelecionada?.nome
                                    ?: "Selecione",

                            opcoes =
                                contas.map { conta ->
                                    conta.nome
                                },

                            modifier =
                                Modifier.fillMaxWidth(),

                            onSelecionar = { nomeConta ->

                                contaSelecionada =
                                    contas.firstOrNull { conta ->
                                        conta.nome == nomeConta
                                    }

                                mensagem = ""
                            }
                        )
                    }


                    // SAÍDA NO CRÉDITO

                    if (formaPagamento == "Crédito") {

                        Spacer(
                            modifier = Modifier.height(14.dp)
                        )

                        Text(
                            text = "Cartão",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        DropdownBlik(
                            valorSelecionado =
                                cartaoSelecionado?.nome
                                    ?: "Selecione",

                            opcoes =
                                cartoes.map { cartao ->
                                    cartao.nome
                                },

                            modifier =
                                Modifier.fillMaxWidth(),

                            onSelecionar = { nomeCartao ->

                                cartaoSelecionado =
                                    cartoes.firstOrNull { cartao ->
                                        cartao.nome == nomeCartao
                                    }

                                mensagem = ""
                            }
                        )

                        Spacer(
                            modifier = Modifier.height(14.dp)
                        )

                        Text(
                            text = "Parcelamento",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        DropdownBlik(
                            valorSelecionado =
                                if (quantidadeParcelas == "1") {
                                    "1x - À vista"
                                } else {
                                    "${quantidadeParcelas}x"
                                },

                            opcoes =
                                (1..24).map { numero ->

                                    if (numero == 1) {
                                        "1x - À vista"
                                    } else {
                                        "${numero}x"
                                    }
                                },

                            modifier =
                                Modifier.fillMaxWidth(),

                            onSelecionar = { opcao ->

                                val numeroParcelas =
                                    if (opcao.startsWith("1x")) {

                                        1

                                    } else {

                                        opcao
                                            .removeSuffix("x")
                                            .toIntOrNull()
                                            ?: 1
                                    }


                                quantidadeParcelas =
                                    numeroParcelas.toString()

                                mensagem = ""
                            }
                        )
                    }


                    if (formaPagamento.isNotBlank()) {

                        Spacer(
                            modifier = Modifier.height(14.dp)
                        )

                        Text(
                            text = "Categoria",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        DropdownBlik(
                            valorSelecionado =
                                categoriaSelecionada?.nome
                                    ?: "Selecione",

                            opcoes =
                                categorias.map { categoria ->
                                    categoria.nome
                                },

                            modifier =
                                Modifier.fillMaxWidth(),

                            onSelecionar = { nomeCategoria ->

                                categoriaSelecionada =
                                    categorias.firstOrNull { categoria ->
                                        categoria.nome == nomeCategoria
                                    }

                                mensagem = ""
                            }
                        )
                    }
                }


                // -------------------------
                // TRANSFERÊNCIA
                // -------------------------

                if (tipo == "Transferência") {

                    Spacer(
                        modifier = Modifier.height(14.dp)
                    )

                    Text(
                        text = "Conta de origem",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    DropdownBlik(
                        valorSelecionado =
                            contaSelecionada?.nome
                                ?: "Selecione",

                        opcoes =
                            contas.map { conta ->
                                conta.nome
                            },

                        modifier =
                            Modifier.fillMaxWidth(),

                        onSelecionar = { nomeConta ->

                            val novaConta =
                                contas.firstOrNull { conta ->
                                    conta.nome == nomeConta
                                }

                            contaSelecionada =
                                novaConta

                            // Se a nova origem for igual ao destino
                            // anteriormente selecionado, limpa o destino.
                            if (
                                contaDestinoSelecionada?.id ==
                                novaConta?.id
                            ) {
                                contaDestinoSelecionada = null
                            }

                            mensagem = ""
                        }
                    )

                    Spacer(
                        modifier = Modifier.height(14.dp)
                    )

                    Text(
                        text = "Conta de destino",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    DropdownBlik(
                        valorSelecionado =
                            contaDestinoSelecionada?.nome
                                ?: "Selecione",

                        opcoes =
                            contas
                                .filter { conta ->
                                    conta.id !=
                                            contaSelecionada?.id
                                }
                                .map { conta ->
                                    conta.nome
                                },

                        modifier =
                            Modifier.fillMaxWidth(),

                        onSelecionar = { nomeConta ->

                            contaDestinoSelecionada =
                                contas.firstOrNull { conta ->
                                    conta.nome == nomeConta &&
                                            conta.id !=
                                            contaSelecionada?.id
                                }

                            mensagem = ""
                        }
                    )
                }


                // DATA

                if (tipo.isNotBlank()) {

                    Spacer(
                        modifier = Modifier.height(14.dp)
                    )

                    Text(
                        text = "Data",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    androidx.compose.material3.OutlinedButton(
                        onClick = {
                            mostrarCalendario = true
                        },

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(50.dp),

                        shape =
                            androidx.compose.foundation.shape
                                .RoundedCornerShape(14.dp)
                    ) {

                        Text(
                            text = data,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }


                // MENSAGEM

                if (mensagem.isNotBlank()) {

                    Spacer(
                        modifier = Modifier.height(14.dp)
                    )

                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .background(
                                    color =
                                        MaterialTheme.colorScheme
                                            .errorContainer,

                                    shape =
                                        androidx.compose.foundation.shape
                                            .RoundedCornerShape(12.dp)
                                )
                                .padding(
                                    horizontal = 14.dp,
                                    vertical = 10.dp
                                )
                    ) {

                        Text(
                            text = mensagem,
                            fontSize = 13.sp,
                            color =
                                MaterialTheme.colorScheme
                                    .onErrorContainer
                        )
                    }
                }


                // SALVAR

                if (tipo.isNotBlank()) {

                    Spacer(
                        modifier = Modifier.height(24.dp)
                    )

                    Button(
                        onClick = {

                            val valorConvertido =
                                valor
                                    .replace(".", "")
                                    .replace(",", ".")
                                    .toDoubleOrNull()

                            val parcelasConvertidas =
                                quantidadeParcelas
                                    .toIntOrNull()
                                    ?: 1


                            if (descricao.isBlank()) {

                                mensagem =
                                    "Digite a descrição."

                            } else if (
                                valorConvertido == null ||
                                valorConvertido <= 0
                            ) {

                                mensagem =
                                    "Digite um valor válido."

                            } else if (
                                tipo == "Entrada" &&
                                contaSelecionada == null
                            ) {

                                mensagem =
                                    "Selecione a conta."

                            } else if (
                                tipo == "Entrada" &&
                                categoriaSelecionada == null
                            ) {

                                mensagem =
                                    "Selecione a categoria."

                            } else if (
                                tipo == "Saída" &&
                                formaPagamento.isBlank()
                            ) {

                                mensagem =
                                    "Selecione a forma de pagamento."

                            } else if (
                                tipo == "Saída" &&
                                formaPagamento == "Conta" &&
                                contaSelecionada == null
                            ) {

                                mensagem =
                                    "Selecione a conta."

                            } else if (
                                tipo == "Saída" &&
                                formaPagamento == "Crédito" &&
                                cartaoSelecionado == null
                            ) {

                                mensagem =
                                    "Selecione o cartão."

                            } else if (
                                tipo == "Saída" &&
                                categoriaSelecionada == null
                            ) {

                                mensagem =
                                    "Selecione a categoria."

                            } else if (
                                tipo == "Transferência" &&
                                contaSelecionada == null
                            ) {

                                mensagem =
                                    "Selecione a conta de origem."

                            } else if (
                                tipo == "Transferência" &&
                                contaDestinoSelecionada == null
                            ) {

                                mensagem =
                                    "Selecione a conta de destino."

                            } else if (
                                tipo == "Transferência" &&
                                contaSelecionada?.id ==
                                contaDestinoSelecionada?.id
                            ) {

                                mensagem =
                                    "A conta de origem e destino devem ser diferentes."

                            } else {

                                val movimentacao =
                                    Movimentacao(

                                        id =
                                            movimentacaoParaEditar
                                                ?.id
                                                ?: 0,

                                        descricao =
                                            descricao.trim(),

                                        valor =
                                            valorConvertido,

                                        tipo =
                                            tipo,

                                        formaPagamento =
                                            if (tipo == "Saída") {
                                                formaPagamento
                                            } else {
                                                null
                                            },

                                        contaId =
                                            if (
                                                tipo == "Entrada" ||
                                                tipo == "Transferência" ||
                                                (
                                                        tipo == "Saída" &&
                                                                formaPagamento == "Conta"
                                                        )
                                            ) {
                                                contaSelecionada?.id
                                            } else {
                                                null
                                            },

                                        contaNome =
                                            contaSelecionada?.nome,

                                        contaDestinoId =
                                            if (
                                                tipo == "Transferência"
                                            ) {
                                                contaDestinoSelecionada?.id
                                            } else {
                                                null
                                            },

                                        contaDestinoNome =
                                            if (
                                                tipo == "Transferência"
                                            ) {
                                                contaDestinoSelecionada?.nome
                                            } else {
                                                null
                                            },

                                        categoriaId =
                                            if (
                                                tipo != "Transferência"
                                            ) {
                                                categoriaSelecionada?.id
                                            } else {
                                                null
                                            },

                                        categoriaNome =
                                            if (
                                                tipo != "Transferência"
                                            ) {
                                                categoriaSelecionada?.nome
                                            } else {
                                                null
                                            },

                                        cartaoId =
                                            if (
                                                tipo == "Saída" &&
                                                formaPagamento == "Crédito"
                                            ) {
                                                cartaoSelecionado?.id
                                            } else {
                                                null
                                            },

                                        cartaoNome =
                                            if (
                                                tipo == "Saída" &&
                                                formaPagamento == "Crédito"
                                            ) {
                                                cartaoSelecionado?.nome
                                            } else {
                                                null
                                            },

                                        quantidadeParcelas =
                                            if (
                                                tipo == "Saída" &&
                                                formaPagamento == "Crédito"
                                            ) {
                                                parcelasConvertidas
                                            } else {
                                                1
                                            },

                                        data = data
                                    )

                                onSalvar(movimentacao)
                            }
                        },

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(52.dp),

                        shape =
                            androidx.compose.foundation.shape
                                .RoundedCornerShape(14.dp),

                        colors =
                            androidx.compose.material3.ButtonDefaults
                                .buttonColors(
                                    containerColor =
                                        MaterialTheme.colorScheme.primary,

                                    contentColor =
                                        MaterialTheme.colorScheme.onPrimary
                                ),

                        elevation =
                            androidx.compose.material3.ButtonDefaults
                                .buttonElevation(
                                    defaultElevation = 0.dp,
                                    pressedElevation = 1.dp
                                )
                    ) {

                        Text(
                            text =
                                if (
                                    movimentacaoParaEditar == null
                                ) {
                                    "Salvar movimentação"
                                } else {
                                    "Salvar alterações"
                                },

                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(32.dp)
                )
            }
        }

        // =====================================================
        // CALENDÁRIO
        // =====================================================

        if (mostrarCalendario) {

            val datePickerState =
                rememberDatePickerState()

            DatePickerDialog(
                onDismissRequest = {
                    mostrarCalendario = false
                },

                confirmButton = {

                    TextButton(
                        onClick = {

                            datePickerState
                                .selectedDateMillis
                                ?.let { millis ->

                                    val calendario =
                                        java.util.Calendar.getInstance(
                                            java.util.TimeZone.getTimeZone("UTC")
                                        )
                                    calendario.timeInMillis = millis

                                    val dia =
                                        calendario.get(
                                            java.util.Calendar.DAY_OF_MONTH
                                        )
                                    val mes =
                                        calendario.get(
                                            java.util.Calendar.MONTH
                                        ) + 1
                                    val ano =
                                        calendario.get(
                                            java.util.Calendar.YEAR
                                        )
                                    data =
                                        String.format(
                                            Locale.getDefault(),
                                            "%02d/%02d/%04d",
                                            dia,
                                            mes,
                                            ano
                                        )
                                }

                            mostrarCalendario = false
                        }
                    ) {
                        Text("OK")
                    }
                },

                dismissButton = {

                    TextButton(
                        onClick = {
                            mostrarCalendario = false
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            ) {

                DatePicker(
                    state = datePickerState
                )
            }
        }
    }
}

@Composable
fun ContaItem(
    nome: String,
    saldo: String,
    onClick: () -> Unit
) {

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp)
                .clickable {
                    onClick()
                },

        shape =
            androidx.compose.foundation.shape.RoundedCornerShape(
                18.dp
            ),

        colors =
            androidx.compose.material3.CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surface
            ),

        elevation =
            androidx.compose.material3.CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 14.dp
                    ),

            verticalAlignment =
                androidx.compose.ui.Alignment.CenterVertically
        ) {

            // ÍCONE DA CONTA
            Box(
                modifier =
                    Modifier
                        .size(46.dp)
                        .background(
                            color =
                                MaterialTheme.colorScheme
                                    .primaryContainer,

                            shape =
                                androidx.compose.foundation.shape
                                    .CircleShape
                        ),

                contentAlignment =
                    androidx.compose.ui.Alignment.Center
            ) {

                Text(
                    text = "▣",
                    fontSize = 22.sp,
                    color =
                        MaterialTheme.colorScheme.primary,
                    fontWeight =
                        FontWeight.Bold
                )
            }


            Spacer(
                modifier =
                    Modifier.width(14.dp)
            )


            // NOME DA CONTA
            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text = nome,
                    fontSize = 16.sp,
                    fontWeight =
                        FontWeight.SemiBold,
                    color =
                        MaterialTheme.colorScheme
                            .onSurface
                )

                Spacer(
                    modifier =
                        Modifier.height(2.dp)
                )

                Text(
                    text = "Conta",
                    fontSize = 13.sp,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )
            }


            // SALDO
            Text(
                text = saldo,
                fontSize = 16.sp,
                fontWeight =
                    FontWeight.Bold,
                color =
                    MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun TelaContas(
    contas: List<ContaEntity>,
    onAdicionarConta: (
        String,
        Double,
        (Boolean) -> Unit
    ) -> Unit,
    onEditar: (
        ContaEntity,
        String,
        Double,
        (Boolean) -> Unit
    ) -> Unit,
    onDesativar: (ContaEntity) -> Unit,
    onReativar: (ContaEntity) -> Unit,
    onExcluir: (
        ContaEntity,
        (Boolean, String) -> Unit
    ) -> Unit,
    onVoltar: () -> Unit
) {
    BackHandler {
        onVoltar()
    }

    var nomeNovaConta by remember {
        mutableStateOf("")
    }

    var saldoInicialNovaConta by remember {
        mutableStateOf("")
    }

    var contaParaEditar by remember {
        mutableStateOf<ContaEntity?>(null)
    }
    var novoNomeConta by remember {
        mutableStateOf("")
    }

    var novoSaldoInicial by remember {
        mutableStateOf("")
    }

    var mensagemEdicao by remember {
        mutableStateOf("")
    }

    var mensagem by remember {
        mutableStateOf("")
    }

    var mensagemExclusao by remember {
        mutableStateOf("")
    }

    var contaParaExcluir by remember {
        mutableStateOf<ContaEntity?>(null)
    }

    var mostrarNovaConta by remember {
        mutableStateOf(false)
    }

    var contaSelecionadaAcoes by remember {
        mutableStateOf<ContaEntity?>(null)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),

        containerColor =
            MaterialTheme.colorScheme.background,

        floatingActionButton = {

            androidx.compose.material3.FloatingActionButton(
                onClick = {
                    mensagem = ""
                    mostrarNovaConta = true
                },

                containerColor =
                    MaterialTheme.colorScheme.primary,

                contentColor =
                    MaterialTheme.colorScheme.onPrimary,

                shape =
                    androidx.compose.foundation.shape.CircleShape
            ) {

                Text(
                    text = "+",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(20.dp)
                .fillMaxSize()
        ) {

            item {

                Text(
                    text = "Contas",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color =
                        MaterialTheme.colorScheme.onBackground
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = "Gerencie suas contas e saldos",
                    fontSize = 14.sp,
                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
                )
            }

            items(
                items = contas,
                key = { contas ->
                    contas.id
                }
            ) { conta ->

                Card(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clickable {
                                contaSelecionadaAcoes = conta
                            },

                    shape =
                        androidx.compose.foundation.shape.RoundedCornerShape(
                            18.dp
                        ),

                    colors =
                        androidx.compose.material3.CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme.colorScheme.surface
                        ),

                    elevation =
                        androidx.compose.material3.CardDefaults.cardElevation(
                            defaultElevation = 2.dp
                        )
                ) {

                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),

                        horizontalArrangement =
                            Arrangement.SpaceBetween,

                        verticalAlignment =
                            androidx.compose.ui.Alignment.CenterVertically
                    ) {

                        Column(
                            modifier =
                                Modifier.weight(1f)
                        ) {

                            Text(
                                text = conta.nome,
                                fontSize = 17.sp,
                                fontWeight =
                                    FontWeight.SemiBold
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(4.dp)
                            )

                            Text(
                                text =
                                    "Saldo inicial: " +
                                            formatarDinheiro(
                                                conta.saldoInicial
                                            ),

                                fontSize = 13.sp,

                                color =
                                    MaterialTheme.colorScheme
                                        .onSurfaceVariant
                            )
                        }


                        Box(
                            modifier =
                                Modifier.background(
                                    color =
                                        if (conta.ativa) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme
                                                .surfaceVariant
                                        },

                                    shape =
                                        androidx.compose.foundation.shape
                                            .RoundedCornerShape(
                                                50.dp
                                            )
                                )
                        ) {

                            Text(
                                text =
                                    if (conta.ativa) {
                                        "ATIVA"
                                    } else {
                                        "DESATIVADA"
                                    },

                                modifier =
                                    Modifier.padding(
                                        horizontal = 10.dp,
                                        vertical = 4.dp
                                    ),

                                fontSize = 11.sp,

                                fontWeight =
                                    FontWeight.Bold,

                                color =
                                    if (conta.ativa) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme
                                            .onSurfaceVariant
                                    }
                            )
                        }
                    }
                }
            }
        }
    }

    // =====================================================
// NOVA CONTA
// =====================================================

    if (mostrarNovaConta) {

        AlertDialog(
            onDismissRequest = {
                mostrarNovaConta = false
                mensagem = ""
            },

            title = {
                Text(
                    text = "Nova conta",
                    fontWeight = FontWeight.Bold
                )
            },

            text = {

                Column {

                    OutlinedTextField(
                        value = nomeNovaConta,

                        onValueChange = {
                            nomeNovaConta = it
                            mensagem = ""
                        },

                        label = {
                            Text("Nome da conta")
                        },

                        singleLine = true,

                        modifier =
                            Modifier.fillMaxWidth()
                    )


                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )


                    OutlinedTextField(
                        value =
                            saldoInicialNovaConta,

                        onValueChange = {
                            saldoInicialNovaConta = it
                            mensagem = ""
                        },

                        label = {
                            Text("Saldo inicial")
                        },

                        placeholder = {
                            Text("Ex.: 1.500,00")
                        },

                        singleLine = true,

                        modifier =
                            Modifier.fillMaxWidth()
                    )


                    if (mensagem.isNotBlank()) {

                        Spacer(
                            modifier =
                                Modifier.height(10.dp)
                        )

                        Text(
                            text = mensagem,
                            fontSize = 13.sp,
                            color =
                                MaterialTheme.colorScheme.error
                        )
                    }
                }
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        val saldoConvertido =
                            saldoInicialNovaConta
                                .replace(".", "")
                                .replace(",", ".")
                                .toDoubleOrNull()


                        if (nomeNovaConta.isBlank()) {

                            mensagem =
                                "Digite o nome da conta."

                        } else if (
                            saldoConvertido == null
                        ) {

                            mensagem =
                                "Digite um saldo inicial válido."

                        } else {

                            onAdicionarConta(
                                nomeNovaConta,
                                saldoConvertido
                            ) { sucesso ->

                                if (sucesso) {

                                    nomeNovaConta = ""
                                    saldoInicialNovaConta = ""
                                    mensagem = ""

                                    mostrarNovaConta =
                                        false

                                } else {

                                    mensagem =
                                        "Já existe uma conta com esse nome."
                                }
                            }
                        }
                    }
                ) {

                    Text("Adicionar")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        mostrarNovaConta = false
                        mensagem = ""
                    }
                ) {

                    Text("Cancelar")
                }
            }
        )
    }

    contaSelecionadaAcoes?.let { conta ->

        AlertDialog(
            onDismissRequest = {
                contaSelecionadaAcoes = null
            },

            title = {
                Text(
                    text = conta.nome,
                    fontWeight = FontWeight.Bold
                )
            },

            text = {

                Column {

                    TextButton(
                        onClick = {

                            contaSelecionadaAcoes = null

                            contaParaEditar = conta

                            novoNomeConta =
                                conta.nome

                            novoSaldoInicial =
                                conta.saldoInicial
                                    .toString()
                                    .replace(".", ",")

                            mensagemEdicao = ""
                        },

                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Text("Editar")
                    }


                    TextButton(
                        onClick = {

                            contaSelecionadaAcoes =
                                null

                            if (conta.ativa) {
                                onDesativar(conta)
                            } else {
                                onReativar(conta)
                            }
                        },

                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Text(
                            if (conta.ativa) {
                                "Desativar"
                            } else {
                                "Reativar"
                            }
                        )
                    }


                    TextButton(
                        onClick = {

                            contaSelecionadaAcoes =
                                null

                            contaParaExcluir =
                                conta
                        },

                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Text(
                            text = "Excluir",
                            color =
                                MaterialTheme.colorScheme.error
                        )
                    }
                }
            },

            confirmButton = {},

            dismissButton = {

                TextButton(
                    onClick = {
                        contaSelecionadaAcoes = null
                    }
                ) {

                    Text("Cancelar")
                }
            }
        )
    }

    contaParaExcluir?.let { conta ->
        AlertDialog(
            onDismissRequest = {
                contaParaExcluir = null
            },

            title = {
                Text(
                    text = "Excluir conta?"
                )
            },

            text = {
                Text(
                    text = "Deseja excluir a conta \"${conta.nome}\" definitivamente? Esta ação não pode ser desfeita."
                )
            },

            confirmButton = {
                TextButton(
                    onClick = {

                        onExcluir(conta) { sucesso, mensagem ->

                            if (sucesso) {

                                mensagemExclusao =
                                    "Conta excluída com sucesso."

                            } else {

                                mensagemExclusao =
                                    mensagem
                            }

                            contaParaExcluir =
                                null
                        }
                    }
                ) {
                    Text("Excluir")
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        contaParaExcluir = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )}
    if (mensagemExclusao.isNotBlank()) {
        AlertDialog(
            onDismissRequest = {
                mensagemExclusao = ""
            },
            title = {
                Text(
                    text =
                        if (
                            mensagemExclusao.startsWith("Conta excluída")
                        ) {
                            "Conta excluída"
                        } else {
                            "Não é possível excluir"
                        }
                )
            },
            text = {
                Text(
                    text = mensagemExclusao
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mensagemExclusao = ""
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
    contaParaEditar?.let { conta ->

        AlertDialog(
            onDismissRequest = {
                contaParaEditar = null
                mensagemEdicao = ""
            },

            title = {
                Text("Editar conta")
            },

            text = {

                Column {

                    OutlinedTextField(
                        value = novoNomeConta,

                        onValueChange = {
                            novoNomeConta = it
                            mensagemEdicao = ""
                        },

                        label = {
                            Text("Nome da conta")
                        },

                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    OutlinedTextField(
                        value = novoSaldoInicial,
                        onValueChange = {
                            novoSaldoInicial = it
                            mensagemEdicao = ""
                        },
                        label = {
                            Text("Saldo inicial")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (mensagemEdicao.isNotBlank()) {

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text = mensagemEdicao
                        )
                    }
                }
            },

            confirmButton = {

                TextButton(
                    onClick = {
                        val saldoConvertido =
                            novoSaldoInicial
                                .replace(".", "")
                                .replace(",",".")
                                .toDoubleOrNull()
                        if (novoNomeConta.isBlank()) {

                            mensagemEdicao =
                                "O nome da conta não pode ficar vazio."
                        } else if (saldoConvertido == null) {
                            mensagemEdicao =
                                "Digite um saldo inicial válido."
                        } else {

                            onEditar(
                                conta,
                                novoNomeConta,
                                saldoConvertido
                            ) { sucesso ->

                                if (sucesso) {

                                    contaParaEditar = null
                                    mensagemEdicao = ""

                                } else {

                                    mensagemEdicao =
                                        "Já existe outra conta com esse nome."
                                }
                            }
                        }
                    }
                ) {
                    Text("Salvar")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        contaParaEditar = null
                        mensagemEdicao = ""
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun TelaCartoes(
    cartoes: List<CartaoComConta>,
    contas: List<ContaEntity>,
    parcelasCartao: List<ParcelaCartaoComDetalhes>,
    pagamentosFatura: List<PagamentoFaturaEntity>,

    onAdicionar: (
        String,
        Double,
        Int,
        Int,
        Int,
        (Boolean) -> Unit
    ) -> Unit,

    onEditar: (
        CartaoComConta,
        String,
        Double,
        Int,
        Int,
        Int,
        (Boolean) -> Unit
    ) -> Unit,

    onExcluir: (
        CartaoComConta,
        (Boolean, String) -> Unit
    ) -> Unit,

    onVoltar: () -> Unit
) {
    BackHandler {
        onVoltar()
    }

    var nomeCartao by remember {
        mutableStateOf("")
    }

    var limiteCartao by remember {
        mutableStateOf("")
    }

    var fechamentoCartao by remember {
        mutableStateOf("")
    }

    var vencimentoCartao by remember {
        mutableStateOf("")
    }
    var contaSelecionada by remember {
        mutableStateOf<ContaEntity?>(null)
    }
    var mensagem by remember {
        mutableStateOf("")
    }

    var cartaoParaEditar by remember {
        mutableStateOf<CartaoComConta?>(null)
    }

    var cartaoParaExcluir by remember {
        mutableStateOf<CartaoComConta?>(null)
    }

    var mensagemExclusao by remember {
        mutableStateOf("")
    }

    var novoNomeCartao by remember {
        mutableStateOf("")
    }
    var novoLimiteCartao by remember {
        mutableStateOf("")
    }
    var novoFechamentoCartao by remember {
        mutableStateOf("")
    }
    var novoVencimentoCartao by remember {
        mutableStateOf("")
    }
    var novaContaCartao by remember {
        mutableStateOf<ContaEntity?>(null)
    }

    var mensagemEdicao by remember {
        mutableStateOf("")
    }
    var mostrarNovoCartao by remember {
        mutableStateOf(false)
    }
    var cartaoSelecionadoAcoes by remember {
        mutableStateOf<CartaoComConta?>(null)
    }


    LaunchedEffect(contas) {
        if (
            contas.isNotEmpty() &&
            contaSelecionada == null
        ) {
            contaSelecionada = contas.first()
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),

        containerColor =
            MaterialTheme.colorScheme.background,

        floatingActionButton = {

            androidx.compose.material3.FloatingActionButton(
                onClick = {
                    mensagem = ""

                    if (contaSelecionada == null && contas.isNotEmpty()) {
                        contaSelecionada = contas.first()
                    }

                    mostrarNovoCartao = true
                },

                containerColor =
                    MaterialTheme.colorScheme.primary,

                contentColor =
                    MaterialTheme.colorScheme.onPrimary,

                shape =
                    androidx.compose.foundation.shape.CircleShape
            ) {

                Text(
                    text = "+",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(20.dp)
                .fillMaxSize()
        ) {

            item {

                Text(
                    text = "Cartões",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color =
                        MaterialTheme.colorScheme.onBackground
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = "Gerencie seus cartões e limites",
                    fontSize = 14.sp,
                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
                )
            }

            if (cartoes.isEmpty()) {

                item {

                    Text(
                        text = "Nenhum cartão cadastrado."
                    )

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )
                }

            } else {

                items(
                    items = cartoes,
                    key = { cartao ->
                        cartao.id
                    }
                ) { cartao ->
                    val limiteUtilizado =
                        calcularLimiteUtilizadoCartao(
                            cartaoId = cartao.id,
                            parcelasCartao = parcelasCartao,
                            pagamentosFatura = pagamentosFatura
                        )


                    val limiteDisponivel =
                        (
                                cartao.limite -
                                        limiteUtilizado
                                )
                            .coerceAtLeast(0.0)


                    val percentualUso =
                        if (cartao.limite > 0.0) {

                            limiteUtilizado /
                                    cartao.limite

                        } else {
                            0.0
                        }


                    val progressoBarra =
                        percentualUso
                            .coerceIn(
                                0.0,
                                1.0
                            )
                            .toFloat()


                    val percentualExibido =
                        (percentualUso * 100)
                            .toInt()


                    val corBarra =
                        when {

                            percentualUso >= 0.90 ->
                                MaterialTheme.colorScheme.error

                            percentualUso >= 0.70 ->
                                MaterialTheme.colorScheme.tertiary

                            else ->
                                MaterialTheme.colorScheme.primary
                        }


                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .clickable {
                                    cartaoSelecionadoAcoes = cartao
                                },

                        shape =
                            androidx.compose.foundation.shape.RoundedCornerShape(
                                18.dp
                            ),

                        colors =
                            androidx.compose.material3.CardDefaults.cardColors(
                                containerColor =
                                    MaterialTheme.colorScheme.surface
                            ),

                        elevation =
                            androidx.compose.material3.CardDefaults.cardElevation(
                                defaultElevation = 2.dp
                            )
                    ) {

                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                        ) {

                            // CABEÇALHO
                            Row(
                                modifier =
                                    Modifier.fillMaxWidth(),

                                verticalAlignment =
                                    androidx.compose.ui.Alignment.CenterVertically
                            ) {

                                Box(
                                    modifier =
                                        Modifier
                                            .size(42.dp)
                                            .background(
                                                color =
                                                    MaterialTheme.colorScheme
                                                        .primaryContainer,

                                                shape =
                                                    androidx.compose.foundation.shape
                                                        .CircleShape
                                            ),

                                    contentAlignment =
                                        androidx.compose.ui.Alignment.Center
                                ) {

                                    Text(
                                        text =
                                            cartao.nome
                                                .take(1)
                                                .uppercase(),

                                        fontSize = 17.sp,
                                        fontWeight =
                                            FontWeight.Bold,

                                        color =
                                            MaterialTheme.colorScheme.primary
                                    )
                                }


                                Spacer(
                                    modifier =
                                        Modifier.width(12.dp)
                                )


                                Column(
                                    modifier =
                                        Modifier.weight(1f)
                                ) {

                                    Text(
                                        text = cartao.nome,
                                        fontSize = 17.sp,
                                        fontWeight =
                                            FontWeight.SemiBold
                                    )

                                    Spacer(
                                        modifier =
                                            Modifier.height(2.dp)
                                    )

                                    Text(
                                        text =
                                            "Conta: ${cartao.contaNome}",

                                        fontSize = 13.sp,

                                        color =
                                            MaterialTheme.colorScheme
                                                .onSurfaceVariant
                                    )
                                }
                            }


                            Spacer(
                                modifier =
                                    Modifier.height(16.dp)
                            )


                            // LIMITE
                            Row(
                                modifier =
                                    Modifier.fillMaxWidth(),

                                horizontalArrangement =
                                    Arrangement.SpaceBetween,

                                verticalAlignment =
                                    androidx.compose.ui.Alignment.CenterVertically
                            ) {

                                Text(
                                    text = "Limite",
                                    fontSize = 13.sp,
                                    color =
                                        MaterialTheme.colorScheme
                                            .onSurfaceVariant
                                )

                                Text(
                                    text =
                                        formatarDinheiro(
                                            cartao.limite
                                        ),

                                    fontSize = 17.sp,
                                    fontWeight =
                                        FontWeight.Bold,

                                    color =
                                        MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(
                                modifier = Modifier.height(12.dp)
                            )


                            Row(
                                modifier =
                                    Modifier.fillMaxWidth(),

                                horizontalArrangement =
                                    Arrangement.SpaceBetween
                            ) {

                                Text(
                                    text = "Utilizado",
                                    fontSize = 12.sp,
                                    color =
                                        MaterialTheme.colorScheme
                                            .onSurfaceVariant
                                )


                                Text(
                                    text =
                                        "${formatarDinheiro(limiteUtilizado)} " +
                                                "• $percentualExibido%",

                                    fontSize = 12.sp,

                                    fontWeight =
                                        FontWeight.SemiBold,

                                    color = corBarra
                                )
                            }


                            Spacer(
                                modifier = Modifier.height(6.dp)
                            )


                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .background(
                                            color =
                                                MaterialTheme.colorScheme
                                                    .surfaceVariant,
                                            shape =
                                                androidx.compose.foundation.shape
                                                    .RoundedCornerShape(50.dp)
                                        )
                            ) {

                                if (progressoBarra > 0f) {

                                    Box(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth(
                                                    progressoBarra
                                                )
                                                .height(8.dp)
                                                .background(
                                                    color = corBarra,
                                                    shape =
                                                        androidx.compose.foundation.shape
                                                            .RoundedCornerShape(50.dp)
                                                )
                                    )
                                }
                            }


                            Spacer(
                                modifier = Modifier.height(7.dp)
                            )


                            Row(
                                modifier =
                                    Modifier.fillMaxWidth(),

                                horizontalArrangement =
                                    Arrangement.SpaceBetween
                            ) {

                                Text(
                                    text = "Disponível",
                                    fontSize = 12.sp,
                                    color =
                                        MaterialTheme.colorScheme
                                            .onSurfaceVariant
                                )


                                Text(
                                    text =
                                        formatarDinheiro(
                                            limiteDisponivel
                                        ),

                                    fontSize = 12.sp,

                                    fontWeight =
                                        FontWeight.Medium,

                                    color =
                                        MaterialTheme.colorScheme
                                            .onSurface
                                )
                            }


                            Spacer(
                                modifier =
                                    Modifier.height(12.dp)
                            )


                            // FECHAMENTO E VENCIMENTO
                            Row(
                                modifier =
                                    Modifier.fillMaxWidth(),

                                horizontalArrangement =
                                    Arrangement.SpaceBetween
                            ) {

                                Text(
                                    text =
                                        "Fecha dia ${cartao.diaFechamento}",

                                    fontSize = 13.sp,
                                    color =
                                        MaterialTheme.colorScheme
                                            .onSurfaceVariant
                                )

                                Text(
                                    text =
                                        "Vence dia ${cartao.diaVencimento}",

                                    fontSize = 13.sp,
                                    color =
                                        MaterialTheme.colorScheme
                                            .onSurfaceVariant
                                )
                            }
                        }
                    }
                }

            }
        }

        if (mostrarNovoCartao) {

            AlertDialog(
                onDismissRequest = {
                    mostrarNovoCartao = false
                    mensagem = ""
                },

                title = {
                    Text(
                        text = "Novo cartão",
                        fontWeight = FontWeight.Bold
                    )
                },

                text = {

                    Column {

                        OutlinedTextField(
                            value = nomeCartao,

                            onValueChange = {
                                nomeCartao = it
                                mensagem = ""
                            },

                            label = {
                                Text("Nome do cartão")
                            },

                            singleLine = true,

                            shape =
                                androidx.compose.foundation.shape
                                    .RoundedCornerShape(14.dp),

                            modifier =
                                Modifier.fillMaxWidth()
                        )

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        OutlinedTextField(
                            value = limiteCartao,

                            onValueChange = {
                                limiteCartao = it
                                mensagem = ""
                            },

                            label = {
                                Text("Limite")
                            },

                            placeholder = {
                                Text("Ex.: 5.000,00")
                            },

                            singleLine = true,

                            shape =
                                androidx.compose.foundation.shape
                                    .RoundedCornerShape(14.dp),

                            modifier =
                                Modifier.fillMaxWidth()
                        )

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            OutlinedTextField(
                                value = fechamentoCartao,

                                onValueChange = {
                                    fechamentoCartao = it
                                    mensagem = ""
                                },

                                label = {
                                    Text("Fechamento")
                                },

                                placeholder = {
                                    Text("10")
                                },

                                singleLine = true,

                                shape =
                                    androidx.compose.foundation.shape
                                        .RoundedCornerShape(14.dp),

                                modifier =
                                    Modifier.weight(1f)
                            )

                            Spacer(
                                modifier = Modifier.width(10.dp)
                            )

                            OutlinedTextField(
                                value = vencimentoCartao,

                                onValueChange = {
                                    vencimentoCartao = it
                                    mensagem = ""
                                },

                                label = {
                                    Text("Vencimento")
                                },

                                placeholder = {
                                    Text("17")
                                },

                                singleLine = true,

                                shape =
                                    androidx.compose.foundation.shape
                                        .RoundedCornerShape(14.dp),

                                modifier =
                                    Modifier.weight(1f)
                            )
                        }

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Text(
                            text = "Conta vinculada",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        DropdownBlik(
                            valorSelecionado =
                                contaSelecionada?.nome
                                    ?: "Selecione",

                            opcoes =
                                contas.map { conta ->
                                    conta.nome
                                },

                            modifier =
                                Modifier.fillMaxWidth(),

                            onSelecionar = { nomeConta ->

                                contaSelecionada =
                                    contas.firstOrNull { conta ->
                                        conta.nome == nomeConta
                                    }

                                mensagem = ""
                            }
                        )

                        if (mensagem.isNotBlank()) {

                            Spacer(
                                modifier = Modifier.height(10.dp)
                            )

                            Text(
                                text = mensagem,
                                fontSize = 13.sp,
                                color =
                                    MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },

                confirmButton = {

                    TextButton(
                        onClick = {

                            val limiteConvertido =
                                limiteCartao
                                    .replace(".", "")
                                    .replace(",", ".")
                                    .toDoubleOrNull()

                            val fechamentoConvertido =
                                fechamentoCartao.toIntOrNull()

                            val vencimentoConvertido =
                                vencimentoCartao.toIntOrNull()

                            val contaEscolhida =
                                contaSelecionada


                            if (nomeCartao.isBlank()) {

                                mensagem =
                                    "Digite o nome do cartão."

                            } else if (
                                limiteConvertido == null
                            ) {

                                mensagem =
                                    "Digite um limite válido."

                            } else if (
                                fechamentoConvertido == null ||
                                fechamentoConvertido !in 1..31
                            ) {

                                mensagem =
                                    "O dia de fechamento deve estar entre 1 e 31."

                            } else if (
                                vencimentoConvertido == null ||
                                vencimentoConvertido !in 1..31
                            ) {

                                mensagem =
                                    "O dia de vencimento deve estar entre 1 e 31."

                            } else if (
                                contaEscolhida == null
                            ) {

                                mensagem =
                                    "Selecione uma conta."

                            } else {

                                onAdicionar(
                                    nomeCartao,
                                    limiteConvertido,
                                    fechamentoConvertido,
                                    vencimentoConvertido,
                                    contaEscolhida.id
                                ) { sucesso ->

                                    if (sucesso) {

                                        nomeCartao = ""
                                        limiteCartao = ""
                                        fechamentoCartao = ""
                                        vencimentoCartao = ""
                                        mensagem = ""

                                        mostrarNovoCartao = false

                                    } else {

                                        mensagem =
                                            "Já existe um cartão com esse nome."
                                    }
                                }
                            }
                        }
                    ) {

                        Text("Adicionar")
                    }
                },

                dismissButton = {

                    TextButton(
                        onClick = {
                            mostrarNovoCartao = false
                            mensagem = ""
                        }
                    ) {

                        Text("Cancelar")
                    }
                }
            )
        }

        cartaoSelecionadoAcoes?.let { cartao ->

            AlertDialog(
                onDismissRequest = {
                    cartaoSelecionadoAcoes = null
                },

                title = {

                    Text(
                        text = cartao.nome,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },

                text = {

                    Column {

                        Text(
                            text = "Escolha o que deseja fazer com este cartão.",
                            fontSize = 13.sp,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )


                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )


                        androidx.compose.material3.FilledTonalButton(
                            onClick = {

                                cartaoSelecionadoAcoes =
                                    null

                                cartaoParaEditar =
                                    cartao

                                novoNomeCartao =
                                    cartao.nome

                                novoLimiteCartao =
                                    cartao.limite
                                        .toString()
                                        .replace(".", ",")

                                novoFechamentoCartao =
                                    cartao.diaFechamento
                                        .toString()

                                novoVencimentoCartao =
                                    cartao.diaVencimento
                                        .toString()

                                novaContaCartao =
                                    contas.find { conta ->
                                        conta.id ==
                                                cartao.contaId
                                    }

                                mensagemEdicao = ""
                            },

                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),

                            shape =
                                androidx.compose.foundation.shape
                                    .RoundedCornerShape(14.dp)
                        ) {

                            Text(
                                text = "Editar cartão",
                                fontWeight = FontWeight.SemiBold
                            )
                        }


                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )


                        androidx.compose.material3.OutlinedButton(
                            onClick = {

                                cartaoSelecionadoAcoes =
                                    null

                                cartaoParaExcluir =
                                    cartao
                            },

                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),

                            shape =
                                androidx.compose.foundation.shape
                                    .RoundedCornerShape(14.dp),

                            colors =
                                androidx.compose.material3.ButtonDefaults
                                    .outlinedButtonColors(
                                        contentColor =
                                            MaterialTheme.colorScheme.error
                                    ),

                            border =
                                androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color =
                                        MaterialTheme.colorScheme
                                            .error
                                            .copy(alpha = 0.35f)
                                )
                        ) {

                            Text(
                                text = "Excluir cartão",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },

                confirmButton = {},

                dismissButton = {

                    TextButton(
                        onClick = {
                            cartaoSelecionadoAcoes =
                                null
                        }
                    ) {

                        Text("Cancelar")
                    }
                }
            )
        }

        cartaoParaEditar?.let { cartao ->

            AlertDialog(
                onDismissRequest = {
                    cartaoParaEditar = null
                    mensagemEdicao = ""
                },

                title = {
                    Text("Editar cartão")
                },

                text = {

                    Column {

                        OutlinedTextField(
                            value = novoNomeCartao,
                            onValueChange = {
                                novoNomeCartao = it
                                mensagemEdicao = ""
                            },
                            label = {
                                Text("Nome do cartão")
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        OutlinedTextField(
                            value = novoLimiteCartao,
                            onValueChange = {
                                novoLimiteCartao = it
                                mensagemEdicao = ""
                            },
                            label = {
                                Text("Limite")
                            },

                            singleLine = true,

                            shape =
                                androidx.compose.foundation.shape
                                    .RoundedCornerShape(14.dp),

                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        OutlinedTextField(
                            value = novoFechamentoCartao,
                            onValueChange = {
                                novoFechamentoCartao = it
                                mensagemEdicao = ""
                            },
                            label = {
                                Text("Dia de fechamento")
                            },
                            singleLine = true,

                            shape =
                                androidx.compose.foundation.shape
                                    .RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        OutlinedTextField(
                            value = novoVencimentoCartao,
                            onValueChange = {
                                novoVencimentoCartao = it
                                mensagemEdicao = ""
                            },
                            label = {
                                Text("Dia de vencimento")
                            },
                            singleLine = true,

                            shape =
                                androidx.compose.foundation.shape
                                    .RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Text(
                            text = "Conta vinculada",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        DropdownBlik(
                            valorSelecionado =
                                novaContaCartao?.nome
                                    ?: "Selecione",

                            opcoes =
                                contas.map { conta ->
                                    conta.nome
                                },

                            modifier =
                                Modifier.fillMaxWidth(),

                            onSelecionar = { nomeConta ->

                                novaContaCartao =
                                    contas.firstOrNull { conta ->
                                        conta.nome == nomeConta
                                    }

                                mensagemEdicao = ""
                            }
                        )

                        if (mensagemEdicao.isNotBlank()) {

                            Spacer(
                                modifier = Modifier.height(10.dp)
                            )

                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .background(
                                            color =
                                                MaterialTheme.colorScheme.errorContainer,

                                            shape =
                                                androidx.compose.foundation.shape
                                                    .RoundedCornerShape(12.dp)
                                        )
                                        .padding(
                                            horizontal = 14.dp,
                                            vertical = 10.dp
                                        )
                            ) {

                                Text(
                                    text = mensagemEdicao,
                                    fontSize = 13.sp,
                                    color =
                                        MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                },

                confirmButton = {

                    TextButton(
                        onClick = {

                            val limiteConvertido =
                                novoLimiteCartao
                                    .replace(".", "")
                                    .replace(",", ".")
                                    .toDoubleOrNull()

                            val fechamentoConvertido =
                                novoFechamentoCartao.toIntOrNull()

                            val vencimentoConvertido =
                                novoVencimentoCartao.toIntOrNull()

                            val contaEscolhida =
                                novaContaCartao

                            if (novoNomeCartao.isBlank()) {

                                mensagemEdicao =
                                    "Digite o nome do cartão."

                            } else if (limiteConvertido == null) {

                                mensagemEdicao =
                                    "Digite um limite válido."

                            } else if (
                                fechamentoConvertido == null ||
                                fechamentoConvertido !in 1..31
                            ) {

                                mensagemEdicao =
                                    "O dia de fechamento deve estar entre 1 e 31."

                            } else if (
                                vencimentoConvertido == null ||
                                vencimentoConvertido !in 1..31
                            ) {

                                mensagemEdicao =
                                    "O dia de vencimento deve estar entre 1 e 31."

                            } else if (contaEscolhida == null) {

                                mensagemEdicao =
                                    "Selecione uma conta."

                            } else {

                                onEditar(
                                    cartao,
                                    novoNomeCartao,
                                    limiteConvertido,
                                    fechamentoConvertido,
                                    vencimentoConvertido,
                                    contaEscolhida.id
                                ) { sucesso ->

                                    if (sucesso) {

                                        cartaoParaEditar = null
                                        mensagemEdicao = ""

                                    } else {

                                        mensagemEdicao =
                                            "Já existe outro cartão com esse nome."
                                    }
                                }
                            }
                        }
                    ) {
                        Text("Salvar")
                    }
                },

                dismissButton = {

                    TextButton(
                        onClick = {
                            cartaoParaEditar = null
                            mensagemEdicao = ""
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
        cartaoParaExcluir?.let { cartao ->

            AlertDialog(
                onDismissRequest = {
                    cartaoParaExcluir = null
                    mensagemExclusao = ""
                },

                title = {
                    Text("Excluir cartão?")
                },

                text = {

                    Column {

                        Text(
                            text =
                                "Deseja excluir o cartão \"${cartao.nome}\" definitivamente?"
                        )

                        if (mensagemExclusao.isNotBlank()) {

                            Spacer(
                                modifier = Modifier.height(12.dp)
                            )

                            Text(
                                text = mensagemExclusao,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp
                            )
                        }
                    }
                },

                confirmButton = {

                    TextButton(
                        onClick = {

                            onExcluir(cartao) { sucesso, mensagemRetorno ->

                                if (sucesso) {

                                    cartaoParaExcluir = null
                                    mensagemExclusao = ""

                                } else {

                                    mensagemExclusao =
                                        mensagemRetorno
                                }
                            }
                        }
                    ) {
                        Text("Excluir")
                    }
                },

                dismissButton = {

                    TextButton(
                        onClick = {
                            cartaoParaExcluir = null
                            mensagemExclusao = ""
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun TelaCategorias(
    categorias: List<CategoriaEntity>,

    onAdicionar: (
        String,
        (Boolean) -> Unit
    ) -> Unit,

    onEditar: (
        CategoriaEntity,
        String,
        (Boolean) -> Unit
    ) -> Unit,

    onExcluir: (
        CategoriaEntity,
        (Boolean, Int) -> Unit
    ) -> Unit,

    onVoltar: () -> Unit
) {
    BackHandler {
        onVoltar()
    }

    var nomeNovaCategoria by remember {
        mutableStateOf("")
    }

    var mensagem by remember {
        mutableStateOf("")
    }

    var categoriaParaEditar by remember {
        mutableStateOf<CategoriaEntity?>(null)
    }

    var novoNomeCategoria by remember {
        mutableStateOf("")
    }

    var mensagemEdicao by remember {
        mutableStateOf("")
    }

    var categoriaParaExcluir by remember {
        mutableStateOf<CategoriaEntity?>(null)
    }

    var mensagemExclusao by remember {
        mutableStateOf("")
    }
    var mostrarNovaCategoria by remember {
        mutableStateOf(false)
    }
    var categoriaSelecionadaAcoes by remember {
        mutableStateOf<CategoriaEntity?>(null)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),

        containerColor =
            MaterialTheme.colorScheme.background,

        floatingActionButton = {

            androidx.compose.material3.FloatingActionButton(
                onClick = {
                    mensagem = ""
                    mostrarNovaCategoria = true
                },

                containerColor =
                    MaterialTheme.colorScheme.primary,

                contentColor =
                    MaterialTheme.colorScheme.onPrimary,

                shape =
                    androidx.compose.foundation.shape.CircleShape
            ) {

                Text(
                    text = "+",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(20.dp)
                .fillMaxSize()
        ) {
            item {

                Text(
                    text = "Categorias",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color =
                        MaterialTheme.colorScheme.onBackground
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = "Organize suas movimentações",
                    fontSize = 14.sp,
                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
                )
            }

            items(
                items = categorias,
                key = { categoria ->
                    categoria.id
                }
            ) { categoria ->

                Card(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clickable {
                                categoriaSelecionadaAcoes =
                                    categoria
                            },

                    shape =
                        androidx.compose.foundation.shape.RoundedCornerShape(
                            18.dp
                        ),

                    colors =
                        androidx.compose.material3.CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme.colorScheme.surface
                        ),

                    elevation =
                        androidx.compose.material3.CardDefaults.cardElevation(
                            defaultElevation = 2.dp
                        )
                ) {

                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 16.dp,
                                    vertical = 14.dp
                                ),

                        verticalAlignment =
                            androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text(
                            text = categoria.nome,

                            modifier =
                                Modifier.weight(1f),

                            fontSize = 16.sp,

                            fontWeight =
                                FontWeight.SemiBold,

                            color =
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }

    if (mostrarNovaCategoria) {

        AlertDialog(
            onDismissRequest = {
                mostrarNovaCategoria = false
                mensagem = ""
            },

            title = {
                Text(
                    text = "Nova categoria",
                    fontWeight = FontWeight.Bold
                )
            },

            text = {

                Column {

                    OutlinedTextField(
                        value = nomeNovaCategoria,

                        onValueChange = {
                            nomeNovaCategoria = it
                            mensagem = ""
                        },

                        label = {
                            Text("Nome da categoria")
                        },

                        singleLine = true,

                        modifier =
                            Modifier.fillMaxWidth()
                    )


                    if (mensagem.isNotBlank()) {

                        Spacer(
                            modifier = Modifier.height(10.dp)
                        )

                        Text(
                            text = mensagem,
                            fontSize = 13.sp,
                            color =
                                MaterialTheme.colorScheme.error
                        )
                    }
                }
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        if (nomeNovaCategoria.isBlank()) {

                            mensagem =
                                "Digite um nome para a categoria."

                        } else {

                            onAdicionar(
                                nomeNovaCategoria
                            ) { sucesso ->

                                if (sucesso) {

                                    nomeNovaCategoria = ""
                                    mensagem = ""
                                    mostrarNovaCategoria = false

                                } else {

                                    mensagem =
                                        "Já existe uma categoria com esse nome."
                                }
                            }
                        }
                    }
                ) {

                    Text("Adicionar")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        mostrarNovaCategoria = false
                        mensagem = ""
                    }
                ) {

                    Text("Cancelar")
                }
            }
        )
    }

    categoriaSelecionadaAcoes?.let { categoria ->

        AlertDialog(
            onDismissRequest = {
                categoriaSelecionadaAcoes = null
            },

            title = {
                Text(
                    text = categoria.nome,
                    fontWeight = FontWeight.Bold
                )
            },

            text = {

                Column {

                    TextButton(
                        onClick = {

                            categoriaSelecionadaAcoes =
                                null

                            categoriaParaEditar =
                                categoria

                            novoNomeCategoria =
                                categoria.nome

                            mensagemEdicao = ""
                        },

                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Text("Editar")
                    }


                    TextButton(
                        onClick = {

                            categoriaSelecionadaAcoes =
                                null

                            categoriaParaExcluir =
                                categoria
                        },

                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Text(
                            text = "Excluir",
                            color =
                                MaterialTheme.colorScheme.error
                        )
                    }
                }
            },

            confirmButton = {},

            dismissButton = {

                TextButton(
                    onClick = {
                        categoriaSelecionadaAcoes =
                            null
                    }
                ) {

                    Text("Cancelar")
                }
            }
        )
    }


    // EDITAR CATEGORIA

    categoriaParaEditar?.let { categoria ->

        AlertDialog(
            onDismissRequest = {
                categoriaParaEditar = null
                mensagemEdicao = ""
            },

            title = {
                Text("Editar categoria")
            },

            text = {

                Column {

                    OutlinedTextField(
                        value = novoNomeCategoria,

                        onValueChange = {
                            novoNomeCategoria = it
                            mensagemEdicao = ""
                        },

                        label = {
                            Text("Nome da categoria")
                        },

                        modifier = Modifier.fillMaxWidth()
                    )

                    if (mensagemEdicao.isNotBlank()) {

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(mensagemEdicao)
                    }
                }
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        if (novoNomeCategoria.isBlank()) {

                            mensagemEdicao =
                                "O nome não pode ficar vazio."

                        } else {

                            onEditar(
                                categoria,
                                novoNomeCategoria
                            ) { sucesso ->

                                if (sucesso) {

                                    categoriaParaEditar = null

                                } else {

                                    mensagemEdicao =
                                        "Já existe outra categoria com esse nome."
                                }
                            }
                        }
                    }
                ) {
                    Text("Salvar")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        categoriaParaEditar = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }


    // CONFIRMAÇÃO DE EXCLUSÃO

    categoriaParaExcluir?.let { categoria ->

        AlertDialog(
            onDismissRequest = {
                categoriaParaExcluir = null
            },

            title = {
                Text("Excluir categoria?")
            },

            text = {
                Text(
                    "Deseja excluir a categoria " +
                            "\"${categoria.nome}\"?"
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        onExcluir(
                            categoria
                        ) { sucesso, quantidade ->

                            if (sucesso) {

                                mensagemExclusao =
                                    "Categoria excluída."

                            } else {

                                mensagemExclusao =
                                    "Não é possível excluir " +
                                            "\"${categoria.nome}\". " +
                                            "Existem $quantidade movimentação(ões) " +
                                            "vinculada(s) a ela."
                            }

                            categoriaParaExcluir = null
                        }
                    }
                ) {
                    Text("Excluir")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        categoriaParaExcluir = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }


    // RESULTADO DA EXCLUSÃO

    if (mensagemExclusao.isNotBlank()) {

        AlertDialog(
            onDismissRequest = {
                mensagemExclusao = ""
            },

            title = {

                Text(
                    if (
                        mensagemExclusao.startsWith(
                            "Categoria excluída"
                        )
                    ) {
                        "Categoria excluída"
                    } else {
                        "Não é possível excluir"
                    }
                )
            },

            text = {
                Text(mensagemExclusao)
            },

            confirmButton = {

                TextButton(
                    onClick = {
                        mensagemExclusao = ""
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun DropdownBlik(
    valorSelecionado: String,
    opcoes: List<String>,
    onSelecionar: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    var aberto by remember {
        mutableStateOf(false)
    }

    var larguraPx by remember {
        mutableStateOf(0)
    }

    val largura =
        with(LocalDensity.current) {
            larguraPx.toDp()
        }


    Box(
        modifier = modifier
    ) {

        androidx.compose.material3.OutlinedButton(
            onClick = {
                aberto = true
            },

            modifier =
                Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordenadas ->

                        larguraPx =
                            coordenadas.size.width
                    },

            shape =
                androidx.compose.foundation.shape
                    .RoundedCornerShape(
                        14.dp
                    )
        ) {

            Text(
                text = valorSelecionado,

                modifier =
                    Modifier.fillMaxWidth(),

                textAlign =
                    TextAlign.Center,

                maxLines = 1
            )
        }


        DropdownMenu(
            expanded = aberto,

            onDismissRequest = {
                aberto = false
            },

            modifier =
                if (larguraPx > 0) {

                    Modifier.width(
                        largura
                    )

                } else {

                    Modifier
                }
        ) {

            opcoes.forEach { opcao ->

                DropdownMenuItem(
                    text = {

                        Text(
                            text = opcao,

                            modifier =
                                Modifier.fillMaxWidth(),

                            textAlign =
                                TextAlign.Center
                        )
                    },

                    onClick = {

                        onSelecionar(
                            opcao
                        )

                        aberto = false
                    }
                )
            }
        }
    }
}

@Composable
fun DetalheMovimentacao(
    titulo: String,
    valor: String
) {

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 5.dp
                ),

        horizontalArrangement =
            Arrangement.SpaceBetween,

        verticalAlignment =
            androidx.compose.ui.Alignment
                .CenterVertically
    ) {

        Text(
            text = titulo,

            fontSize = 13.sp,

            color =
                MaterialTheme.colorScheme
                    .onSurfaceVariant
        )


        Spacer(
            modifier =
                Modifier.width(16.dp)
        )


        Text(
            text = valor,

            modifier =
                Modifier.weight(1f),

            textAlign =
                TextAlign.End,

            fontSize = 13.sp,

            fontWeight =
                FontWeight.Medium,

            color =
                MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun TelaHistorico(
    movimentacoes: List<Movimentacao>,
    onEditar: (Movimentacao) -> Unit,
    onExcluir: (Movimentacao) -> Unit,
    onVoltar: () -> Unit
) {
    BackHandler {
        onVoltar()
    }


    var movimentacaoSelecionada by remember {
        mutableStateOf<Movimentacao?>(null)
    }

    var movimentacaoParaExcluir by remember {
        mutableStateOf<Movimentacao?>(null)
    }

    var mesSelecionado by remember {
        mutableStateOf("Todos")
    }

    var mesSelecionadoNome by remember {
        mutableStateOf("Todos")
    }

    var anoSelecionado by remember {
        mutableStateOf("Todos")
    }

    var contaSelecionada by remember {
        mutableStateOf("Todas")
    }

    var categoriaSelecionada by remember {
        mutableStateOf("Todas")
    }

    var tipoSelecionado by remember {
        mutableStateOf("Todos")
    }

    var mostrarFiltros by remember {
        mutableStateOf(false)
    }


    val meses = listOf(
        "Todos" to "Todos",
        "Janeiro" to "01",
        "Fevereiro" to "02",
        "Março" to "03",
        "Abril" to "04",
        "Maio" to "05",
        "Junho" to "06",
        "Julho" to "07",
        "Agosto" to "08",
        "Setembro" to "09",
        "Outubro" to "10",
        "Novembro" to "11",
        "Dezembro" to "12"
    )

    val anosDisponiveis =
        movimentacoes
            .mapNotNull { movimentacao ->

                val partes =
                    movimentacao.data.split("/")

                if (partes.size == 3) {
                    partes[2]
                } else {
                    null
                }
            }
            .distinct()
            .sortedDescending()

    val anos =
        listOf("Todos") + anosDisponiveis

    val contasDisponiveis: List<String> =
        listOf("Todas") +
                movimentacoes
                    .flatMap { movimentacao ->
                        listOfNotNull(
                            movimentacao.contaNome,
                            movimentacao.contaDestinoNome
                        )
                    }
                    .distinct()
                    .sorted()

    val categoriasDisponiveis: List<String> =
        listOf("Todas") +
                movimentacoes
                    .mapNotNull { movimentacao ->
                        movimentacao.categoriaNome
                    }
                    .distinct()
                    .sorted()

    val tipos = listOf(
        "Todos",
        "Entrada",
        "Saída",
        "Transferência"
    )

    val movimentacoesFiltradas =
        movimentacoes.filter { movimentacao ->

            val partes =
                movimentacao.data.split("/")

            if (partes.size != 3) {

                false

            } else {

                val mesMovimentacao = partes[1]
                val anoMovimentacao = partes[2]

                val mesCorreto =
                    mesSelecionado == "Todos" ||
                            mesMovimentacao == mesSelecionado

                val anoCorreto =
                    anoSelecionado == "Todos" ||
                            anoMovimentacao == anoSelecionado

                val contaCorreta =
                    contaSelecionada == "Todas" ||
                            movimentacao.contaNome == contaSelecionada ||
                            movimentacao.contaDestinoNome == contaSelecionada

                val categoriaCorreta =
                    categoriaSelecionada == "Todas" ||
                            movimentacao.categoriaNome == categoriaSelecionada

                val tipoCorreto =
                    tipoSelecionado == "Todos" ||
                            movimentacao.tipo == tipoSelecionado

                mesCorreto &&
                        anoCorreto &&
                        contaCorreta &&
                        categoriaCorreta &&
                        tipoCorreto
            }
        }

    Scaffold(
        modifier = Modifier.fillMaxSize(),

        containerColor =
            MaterialTheme.colorScheme.background
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .fillMaxSize()
        ) {

            item {

                Spacer(
                    modifier = Modifier.height(20.dp)
                )


                // =============================================
                // CABEÇALHO
                // =============================================

                Text(
                    text = "Histórico",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color =
                        MaterialTheme.colorScheme.onBackground
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = "Acompanhe suas movimentações",
                    fontSize = 14.sp,
                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )


                Spacer(
                    modifier = Modifier.height(20.dp)
                )


                // =============================================
                // MÊS E ANO
                // =============================================

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {

                    DropdownBlik(
                        valorSelecionado =
                            mesSelecionadoNome,

                        opcoes =
                            meses.map { mes ->
                                mes.first
                            },

                        modifier =
                            Modifier.weight(1f),

                        onSelecionar = { nomeMes ->

                            val mesSelecionadoNovo =
                                meses.first { mes ->
                                    mes.first == nomeMes
                                }

                            mesSelecionado =
                                mesSelecionadoNovo.second

                            mesSelecionadoNome =
                                mesSelecionadoNovo.first
                        }
                    )


                    DropdownBlik(
                        valorSelecionado =
                            anoSelecionado,

                        opcoes =
                            anos,

                        modifier =
                            Modifier.weight(1f),

                        onSelecionar = { ano ->

                            anoSelecionado =
                                ano
                        }
                    )
                }


                // =============================================
                // FILTROS ADICIONAIS
                // =============================================

                val quantidadeFiltrosAtivos =
                    listOf(
                        mesSelecionado != "Todos",
                        anoSelecionado != "Todos",
                        contaSelecionada != "Todas",
                        categoriaSelecionada != "Todas",
                        tipoSelecionado != "Todos"
                    ).count { it }


                androidx.compose.material3.FilledTonalButton(
                    onClick = {
                        mostrarFiltros = true
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        androidx.compose.foundation.shape
                            .RoundedCornerShape(14.dp)
                ) {

                    Text(
                        text =
                            if (quantidadeFiltrosAtivos == 0) {
                                "Filtros"
                            } else {
                                "Filtros ($quantidadeFiltrosAtivos)"
                            }
                    )
                }


                Spacer(
                    modifier = Modifier.height(12.dp)
                )


                Text(
                    text =
                        if (movimentacoesFiltradas.size == 1) {
                            "1 movimentação encontrada"
                        } else {
                            "${movimentacoesFiltradas.size} movimentações encontradas"
                        },

                    fontSize = 13.sp,

                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )


                Spacer(
                    modifier = Modifier.height(10.dp)
                )
            }

            if (movimentacoesFiltradas.isEmpty()) {

                item {

                    Text(
                        text = "Nenhuma movimentação encontrada."
                    )

                    Spacer(
                        modifier = Modifier.height(24.dp)
                    )
                }

            } else {

                items(
                    items = movimentacoesFiltradas
                ) { movimentacao ->

                    val corValor =
                        when (movimentacao.tipo) {

                            "Entrada" ->
                                MaterialTheme.colorScheme.primary

                            "Saída" ->
                                MaterialTheme.colorScheme.error

                            else ->
                                MaterialTheme.colorScheme.onSurface
                        }


                    val textoValor =
                        when (movimentacao.tipo) {

                            "Entrada" ->
                                "+ ${formatarDinheiro(movimentacao.valor)}"

                            "Saída" ->
                                "- ${formatarDinheiro(movimentacao.valor)}"

                            else ->
                                formatarDinheiro(movimentacao.valor)
                        }


                    val detalheMovimentacao =
                        when {

                            movimentacao.tipo == "Transferência" -> {

                                "${movimentacao.contaNome ?: "Conta"} → " +
                                        (movimentacao.contaDestinoNome ?: "Conta")
                            }


                            movimentacao.tipo == "Saída" &&
                                    movimentacao.formaPagamento == "Crédito" -> {

                                val parcelas =
                                    if (
                                        movimentacao.quantidadeParcelas > 1
                                    ) {

                                        " • ${movimentacao.quantidadeParcelas}x"

                                    } else {
                                        ""
                                    }

                                "${movimentacao.cartaoNome ?: "Cartão"} • Crédito$parcelas"
                            }


                            movimentacao.tipo == "Saída" -> {

                                "${movimentacao.contaNome ?: "Conta"} • " +
                                        (movimentacao.categoriaNome
                                            ?: "Sem categoria")
                            }


                            movimentacao.tipo == "Entrada" -> {

                                "${movimentacao.contaNome ?: "Conta"} • " +
                                        (movimentacao.categoriaNome
                                            ?: "Sem categoria")
                            }


                            else -> ""
                        }


                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .clickable {
                                    movimentacaoSelecionada =
                                        movimentacao
                                },

                        shape =
                            androidx.compose.foundation.shape
                                .RoundedCornerShape(
                                    18.dp
                                ),

                        colors =
                            androidx.compose.material3.CardDefaults
                                .cardColors(
                                    containerColor =
                                        MaterialTheme.colorScheme.surface
                                ),

                        elevation =
                            androidx.compose.material3.CardDefaults
                                .cardElevation(
                                    defaultElevation = 2.dp
                                )
                    ) {

                        Column(
                            modifier =
                                Modifier.padding(
                                    horizontal = 16.dp,
                                    vertical = 14.dp
                                )
                        ) {


                            // DESCRIÇÃO + VALOR
                            Row(
                                modifier =
                                    Modifier.fillMaxWidth(),

                                horizontalArrangement =
                                    Arrangement.SpaceBetween,

                                verticalAlignment =
                                    androidx.compose.ui.Alignment
                                        .CenterVertically
                            ) {

                                Text(
                                    text =
                                        movimentacao.descricao,

                                    modifier =
                                        Modifier.weight(1f),

                                    fontSize = 16.sp,

                                    fontWeight =
                                        FontWeight.SemiBold,

                                    color =
                                        MaterialTheme.colorScheme
                                            .onSurface
                                )


                                Spacer(
                                    modifier =
                                        Modifier.width(12.dp)
                                )


                                Text(
                                    text = textoValor,

                                    fontSize = 16.sp,

                                    fontWeight =
                                        FontWeight.Bold,

                                    color = corValor
                                )
                            }


                            if (
                                detalheMovimentacao.isNotBlank()
                            ) {

                                Spacer(
                                    modifier =
                                        Modifier.height(5.dp)
                                )


                                Text(
                                    text =
                                        detalheMovimentacao,

                                    fontSize = 13.sp,

                                    color =
                                        MaterialTheme.colorScheme
                                            .onSurfaceVariant
                                )
                            }


                            Spacer(
                                modifier =
                                    Modifier.height(6.dp)
                            )


                            Text(
                                text =
                                    movimentacao.data,

                                fontSize = 12.sp,

                                color =
                                    MaterialTheme.colorScheme
                                        .onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        if (mostrarFiltros) {

            AlertDialog(
                onDismissRequest = {
                    mostrarFiltros = false
                },

                title = {

                    Column {

                        Text(
                            text = "Filtros",
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(2.dp)
                        )

                        Text(
                            text = "Refine as movimentações exibidas",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },

                text = {

                    Column {

                        Text(
                            text = "Conta",
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        DropdownBlik(
                            valorSelecionado =
                                contaSelecionada,

                            opcoes =
                                contasDisponiveis,

                            modifier =
                                Modifier.fillMaxWidth(),

                            onSelecionar = { conta ->

                                contaSelecionada =
                                    conta
                            }
                        )

                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        Text(
                            text = "Categoria",
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        DropdownBlik(
                            valorSelecionado =
                                categoriaSelecionada,

                            opcoes =
                                categoriasDisponiveis,

                            modifier =
                                Modifier.fillMaxWidth(),

                            onSelecionar = { categoria ->

                                categoriaSelecionada =
                                    categoria
                            }
                        )

                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        Text(
                            text = "Tipo",
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        DropdownBlik(
                            valorSelecionado =
                                tipoSelecionado,

                            opcoes =
                                tipos,

                            modifier =
                                Modifier.fillMaxWidth(),

                            onSelecionar = { tipo ->

                                tipoSelecionado =
                                    tipo
                            }
                        )

                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        androidx.compose.material3.FilledTonalButton(
                            onClick = {

                                contaSelecionada = "Todas"
                                categoriaSelecionada = "Todas"
                                tipoSelecionado = "Todos"
                            },

                            modifier =
                                Modifier.fillMaxWidth(),

                            shape =
                                androidx.compose.foundation.shape.RoundedCornerShape(
                                    14.dp
                                )
                        ) {

                            Text("Limpar filtros")
                        }
                    }
                },

                confirmButton = {

                    TextButton(
                        onClick = {
                            mostrarFiltros = false
                        }
                    ) {
                        Text("Aplicar")
                    }
                },

                dismissButton = {

                    TextButton(
                        onClick = {
                            mostrarFiltros = false
                        }
                    ) {
                        Text("Fechar")
                    }
                }
            )
        }
        movimentacaoSelecionada?.let { movimentacao ->

            val corMovimentacao =
                when (movimentacao.tipo) {

                    "Entrada" ->
                        MaterialTheme.colorScheme.primary

                    "Saída" ->
                        MaterialTheme.colorScheme.error

                    else ->
                        MaterialTheme.colorScheme.onSurface
                }


            val corFundoMovimentacao =
                when (movimentacao.tipo) {

                    "Entrada" ->
                        MaterialTheme.colorScheme.primaryContainer

                    "Saída" ->
                        MaterialTheme.colorScheme.errorContainer

                    else ->
                        MaterialTheme.colorScheme.surfaceVariant
                }


            val textoValor =
                when (movimentacao.tipo) {

                    "Entrada" ->
                        "+ ${formatarDinheiro(movimentacao.valor)}"

                    "Saída" ->
                        "- ${formatarDinheiro(movimentacao.valor)}"

                    else ->
                        formatarDinheiro(movimentacao.valor)
                }


            AlertDialog(
                onDismissRequest = {
                    movimentacaoSelecionada = null
                },

                title = {

                    Column {

                        Text(
                            text =
                                movimentacao.descricao,

                            fontSize = 20.sp,

                            fontWeight =
                                FontWeight.Bold
                        )

                        Spacer(
                            modifier =
                                Modifier.height(4.dp)
                        )

                        Text(
                            text =
                                movimentacao.tipo,

                            fontSize = 13.sp,

                            color =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant
                        )
                    }
                },

                text = {

                    Column(
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {


                        // =====================================
                        // VALOR
                        // =====================================

                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color =
                                            corFundoMovimentacao,

                                        shape =
                                            androidx.compose.foundation.shape
                                                .RoundedCornerShape(
                                                    16.dp
                                                )
                                    )
                                    .padding(
                                        horizontal = 16.dp,
                                        vertical = 14.dp
                                    )
                        ) {

                            Column {

                                Text(
                                    text = "Valor",

                                    fontSize = 12.sp,

                                    color =
                                        MaterialTheme.colorScheme
                                            .onSurfaceVariant
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(2.dp)
                                )

                                Text(
                                    text =
                                        textoValor,

                                    fontSize = 22.sp,

                                    fontWeight =
                                        FontWeight.Bold,

                                    color =
                                        corMovimentacao
                                )
                            }
                        }


                        Spacer(
                            modifier =
                                Modifier.height(18.dp)
                        )


                        // =====================================
                        // DETALHES
                        // =====================================

                        when {

                            movimentacao.tipo ==
                                    "Transferência" -> {

                                DetalheMovimentacao(
                                    titulo = "Conta de origem",
                                    valor =
                                        movimentacao.contaNome
                                            ?: "Não informada"
                                )

                                DetalheMovimentacao(
                                    titulo = "Conta de destino",
                                    valor =
                                        movimentacao
                                            .contaDestinoNome
                                            ?: "Não informada"
                                )
                            }


                            movimentacao.tipo == "Saída" &&
                                    movimentacao.formaPagamento ==
                                    "Crédito" -> {

                                DetalheMovimentacao(
                                    titulo = "Cartão",
                                    valor =
                                        movimentacao.cartaoNome
                                            ?: "Não informado"
                                )

                                DetalheMovimentacao(
                                    titulo = "Forma de pagamento",
                                    valor = "Crédito"
                                )

                                DetalheMovimentacao(
                                    titulo = "Parcelamento",
                                    valor =
                                        if (
                                            movimentacao
                                                .quantidadeParcelas > 1
                                        ) {
                                            "${movimentacao.quantidadeParcelas}x"
                                        } else {
                                            "À vista"
                                        }
                                )

                                DetalheMovimentacao(
                                    titulo = "Categoria",
                                    valor =
                                        movimentacao
                                            .categoriaNome
                                            ?: "Não informada"
                                )
                            }


                            else -> {

                                DetalheMovimentacao(
                                    titulo = "Conta",
                                    valor =
                                        movimentacao.contaNome
                                            ?: "Não informada"
                                )

                                DetalheMovimentacao(
                                    titulo = "Categoria",
                                    valor =
                                        movimentacao
                                            .categoriaNome
                                            ?: "Não informada"
                                )


                                if (
                                    movimentacao.tipo ==
                                    "Saída"
                                ) {

                                    DetalheMovimentacao(
                                        titulo =
                                            "Forma de pagamento",

                                        valor =
                                            movimentacao
                                                .formaPagamento
                                                ?: "Não informada"
                                    )
                                }
                            }
                        }


                        DetalheMovimentacao(
                            titulo = "Data",
                            valor = movimentacao.data
                        )
                    }
                },

                confirmButton = {

                    TextButton(
                        onClick = {

                            movimentacaoSelecionada =
                                null

                            onEditar(
                                movimentacao
                            )
                        }
                    ) {

                        Text("Editar")
                    }
                },

                dismissButton = {

                    Row {

                        TextButton(
                            onClick = {

                                movimentacaoParaExcluir =
                                    movimentacao

                                movimentacaoSelecionada =
                                    null
                            }
                        ) {

                            Text(
                                text = "Excluir",
                                color =
                                    MaterialTheme.colorScheme.error
                            )
                        }


                        TextButton(
                            onClick = {
                                movimentacaoSelecionada =
                                    null
                            }
                        ) {

                            Text("Fechar")
                        }
                    }
                }
            )
        }
        movimentacaoParaExcluir?.let { movimentacao ->

            AlertDialog(
                onDismissRequest = {
                    movimentacaoParaExcluir = null
                },

                title = {
                    Text("Excluir movimentação?")
                },

                text = {
                    Text(
                        "Deseja excluir \"${movimentacao.descricao}\" definitivamente?"
                    )
                },

                confirmButton = {

                    TextButton(
                        onClick = {

                            onExcluir(movimentacao)

                            movimentacaoParaExcluir =
                                null
                        }
                    ) {
                        Text("Excluir")
                    }
                },

                dismissButton = {

                    TextButton(
                        onClick = {
                            movimentacaoParaExcluir = null
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaFaturas(
    parcelas: List<ParcelaCartaoComDetalhes>,
    pagamentos: List<PagamentoFaturaComConta>,
    contas: List<ContaEntity>,
    cartoes: List<CartaoComConta>,
    mesInicial: Int?,
    anoInicial: Int?,
    cartaoInicialExpandidoId: Int?,


    onPagar: (
        Int,
        Int,
        Int,
        Int,
        Double,
        String
    ) -> Unit,

    onExcluirPagamento: (
        PagamentoFaturaComConta
    ) -> Unit,

    onVoltar: () -> Unit
) {

    BackHandler {
        onVoltar()
    }

    val calendario =
        java.util.Calendar.getInstance()

    var mesSelecionado by remember(mesInicial) {
        mutableStateOf(mesInicial ?: (
                calendario.get(
                    java.util.Calendar.MONTH
                ) + 1
            )
        )
    }

    var anoSelecionado by remember(anoInicial) {
        mutableStateOf(anoInicial ?:
            calendario.get(
                java.util.Calendar.YEAR
            )
        )
    }


    // PAGAMENTO

    var cartaoParaPagamento by remember {
        mutableStateOf<Int?>(null)
    }

    var nomeCartaoParaPagamento by remember {
        mutableStateOf("")
    }

    var valorRestantePagamento by remember {
        mutableStateOf(0.0)
    }

    var valorPagamento by remember {
        mutableStateOf("")
    }

    var contaPagamento by remember {
        mutableStateOf<ContaEntity?>(null)
    }

    var dataPagamento by remember {
        mutableStateOf(
            SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
            ).format(Date())
        )
    }

    var mostrarCalendarioPagamento by remember {
        mutableStateOf(false)
    }

    var mensagemPagamento by remember {
        mutableStateOf("")
    }

    var pagamentoParaExcluir by remember {
        mutableStateOf<PagamentoFaturaComConta?>(
            null
        )
    }

    var cartaoExpandidoId by remember(cartaoInicialExpandidoId) {
        mutableStateOf(cartaoInicialExpandidoId)
    }

    val nomesMeses =
        listOf(
            "Janeiro",
            "Fevereiro",
            "Março",
            "Abril",
            "Maio",
            "Junho",
            "Julho",
            "Agosto",
            "Setembro",
            "Outubro",
            "Novembro",
            "Dezembro"
        )


    val parcelasDaFatura =
        parcelas.filter { parcela ->

            parcela.mesFatura == mesSelecionado &&
                    parcela.anoFatura == anoSelecionado
        }


    val faturasPorCartao =
        parcelasDaFatura.groupBy { parcela ->
            parcela.cartaoId
        }


    val anosDisponiveis =
        (
                parcelas
                    .map { parcela ->
                        parcela.anoFatura
                    } +
                        listOf(
                            calendario.get(
                                java.util.Calendar.YEAR
                            )
                        )
                )
            .distinct()
            .sortedDescending()


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor =
            MaterialTheme.colorScheme.background
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .fillMaxSize()
        ) {

            // =====================================================
            // CABEÇALHO
            // =====================================================

            item {

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Text(
                    text = "Faturas",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color =
                        MaterialTheme.colorScheme.onBackground
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = "Acompanhe seus cartões e pagamentos",
                    fontSize = 14.sp,
                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
                )


                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {

                    // MÊS
                    DropdownBlik(
                        valorSelecionado =
                            nomesMeses[
                                mesSelecionado - 1
                            ],

                        opcoes =
                            nomesMeses,

                        modifier =
                            Modifier.weight(1f),

                        onSelecionar = { nomeMes ->

                            val indice =
                                nomesMeses.indexOf(
                                    nomeMes
                                )

                            if (indice >= 0) {
                                mesSelecionado =
                                    indice + 1
                            }
                        }
                    )


                    // ANO
                    DropdownBlik(
                        valorSelecionado =
                            anoSelecionado.toString(),

                        opcoes =
                            anosDisponiveis.map { ano ->
                                ano.toString()
                            },

                        modifier =
                            Modifier.weight(1f),

                        onSelecionar = { ano ->

                            ano.toIntOrNull()
                                ?.let { anoConvertido ->

                                    anoSelecionado =
                                        anoConvertido
                                }
                        }
                    )
                }


                Spacer(
                    modifier = Modifier.height(20.dp)
                )
            }


            // =====================================================
            // SEM FATURA
            // =====================================================

            if (parcelasDaFatura.isEmpty()) {

                item {

                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    vertical = 6.dp
                                ),

                        shape =
                            androidx.compose.foundation.shape
                                .RoundedCornerShape(
                                    18.dp
                                ),

                        colors =
                            androidx.compose.material3.CardDefaults
                                .cardColors(
                                    containerColor =
                                        MaterialTheme.colorScheme.surface
                                ),

                        elevation =
                            androidx.compose.material3.CardDefaults
                                .cardElevation(
                                    defaultElevation = 1.dp
                                )
                    ) {

                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = 22.dp,
                                        vertical = 26.dp
                                    ),

                            horizontalAlignment =
                                androidx.compose.ui.Alignment.CenterHorizontally
                        ) {

                            Text(
                                text =
                                    "Nenhuma fatura neste período",

                                fontSize = 17.sp,

                                fontWeight =
                                    FontWeight.SemiBold,

                                textAlign =
                                    TextAlign.Center,

                                color =
                                    MaterialTheme.colorScheme
                                        .onSurface
                            )


                            Spacer(
                                modifier =
                                    Modifier.height(6.dp)
                            )


                            Text(
                                text =
                                    "Não existem compras no crédito " +
                                            "para o período selecionado.",

                                fontSize = 13.sp,

                                textAlign =
                                    TextAlign.Center,

                                color =
                                    MaterialTheme.colorScheme
                                        .onSurfaceVariant
                            )


                            Spacer(
                                modifier =
                                    Modifier.height(16.dp)
                            )


                            Box(
                                modifier =
                                    Modifier.background(
                                        color =
                                            MaterialTheme.colorScheme
                                                .primaryContainer,

                                        shape =
                                            androidx.compose.foundation.shape
                                                .RoundedCornerShape(
                                                    50.dp
                                                )
                                    )
                            ) {

                                Text(
                                    text =
                                        "${nomesMeses[mesSelecionado - 1]} " +
                                                "de $anoSelecionado",

                                    modifier =
                                        Modifier.padding(
                                            horizontal = 14.dp,
                                            vertical = 6.dp
                                        ),

                                    fontSize = 12.sp,

                                    fontWeight =
                                        FontWeight.Medium,

                                    color =
                                        MaterialTheme.colorScheme
                                            .primary
                                )
                            }
                        }
                    }


                    Spacer(
                        modifier =
                            Modifier.height(24.dp)
                    )
                }
            } else {

                // =================================================
                // FATURAS AGRUPADAS POR CARTÃO
                // =================================================

                faturasPorCartao.forEach {
                        (_, parcelasCartao) ->

                    val primeiraParcela =
                        parcelasCartao.first()

                    val cartaoId =
                        primeiraParcela.cartaoId

                    val cartao =
                        cartoes.find { item -> item.id == cartaoId }

                    val parcelasEmAberto =
                        parcelasCartao.filter { parcela ->
                            !parcela.quitadaAnteriormente
                        }

                    val pagamentosDoCartao =
                        pagamentos.filter { pagamento ->
                            pagamento.cartaoId == cartaoId &&
                                    pagamento.mesFatura == mesSelecionado &&
                                    pagamento.anoFatura == anoSelecionado
                        }

                    val totalFatura =
                        parcelasEmAberto
                            .sumOf { parcela ->
                                parcela.valor
                            }

                    val totalPago =
                        pagamentosDoCartao
                            .sumOf { pagamento ->
                                pagamento.valorPago
                            }

                    val restante =
                        (totalFatura - totalPago)
                            .coerceAtLeast(0.0)

                    val fechamento =
                        cartao?.let {
                            criarDataFatura(
                                dia = it.diaFechamento,
                                mes = mesSelecionado,
                                ano = anoSelecionado
                            )
                        }

                    val vencimento =
                        cartao?.let {
                            calcularVencimentoFatura(
                                mesFatura = mesSelecionado,
                                anoFatura = anoSelecionado,
                                diaFechamento = it.diaFechamento,
                                diaVencimento = it.diaVencimento
                            )
                        }

                    val statusFatura =
                        if (cartao != null) {
                            calcularStatusFatura(
                                restante = restante,
                                mesFatura = mesSelecionado,
                                anoFatura = anoSelecionado,
                                diaFechamento = cartao.diaFechamento,
                                diaVencimento = cartao.diaVencimento
                            )
                        } else { "Indisponível"}

                    val estaExpandido =
                        cartaoExpandidoId == cartaoId

                    item {

                        val corStatusFatura =
                            when (statusFatura) {

                                "Vencida" ->
                                    MaterialTheme.colorScheme.error

                                "Fechada" ->
                                    MaterialTheme.colorScheme.tertiary

                                "Paga" ->
                                    MaterialTheme.colorScheme.primary

                                else ->
                                    MaterialTheme.colorScheme.primary
                            }


                        val fundoStatusFatura =
                            when (statusFatura) {

                                "Vencida" ->
                                    MaterialTheme.colorScheme.errorContainer

                                "Fechada" ->
                                    MaterialTheme.colorScheme.tertiaryContainer

                                "Paga" ->
                                    MaterialTheme.colorScheme.primaryContainer

                                else ->
                                    MaterialTheme.colorScheme.primaryContainer
                            }


                        Card(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clickable {

                                        cartaoExpandidoId =
                                            if (estaExpandido) {
                                                null
                                            } else {
                                                cartaoId
                                            }
                                    },

                            shape =
                                androidx.compose.foundation.shape
                                    .RoundedCornerShape(20.dp),

                            colors =
                                androidx.compose.material3.CardDefaults
                                    .cardColors(
                                        containerColor =
                                            MaterialTheme.colorScheme.surface
                                    ),

                            elevation =
                                androidx.compose.material3.CardDefaults
                                    .cardElevation(
                                        defaultElevation = 2.dp
                                    )
                        ) {

                            Column(
                                modifier =
                                    Modifier.padding(18.dp)
                            ) {


                                // =========================================
                                // CARTÃO + STATUS
                                // =========================================

                                Row(
                                    modifier =
                                        Modifier.fillMaxWidth(),

                                    horizontalArrangement =
                                        Arrangement.SpaceBetween,

                                    verticalAlignment =
                                        androidx.compose.ui.Alignment.CenterVertically
                                ) {

                                    Text(
                                        text =
                                            primeiraParcela.cartaoNome,

                                        fontSize = 18.sp,

                                        fontWeight =
                                            FontWeight.Bold
                                    )


                                    Box(
                                        modifier =
                                            Modifier.background(
                                                color =
                                                    fundoStatusFatura,

                                                shape =
                                                    androidx.compose.foundation.shape
                                                        .RoundedCornerShape(50.dp)
                                            )
                                    ) {

                                        Text(
                                            text =
                                                statusFatura.uppercase(),

                                            modifier =
                                                Modifier.padding(
                                                    horizontal = 10.dp,
                                                    vertical = 4.dp
                                                ),

                                            fontSize = 11.sp,

                                            fontWeight =
                                                FontWeight.Bold,

                                            color =
                                                corStatusFatura
                                        )
                                    }
                                }


                                Spacer(
                                    modifier =
                                        Modifier.height(14.dp)
                                )


                                // =========================================
                                // EM ABERTO / PAGA
                                // =========================================

                                if (restante > 0.01) {

                                    Text(
                                        text = "Em aberto",

                                        fontSize = 12.sp,

                                        color =
                                            MaterialTheme.colorScheme
                                                .onSurfaceVariant
                                    )

                                    Spacer(
                                        modifier =
                                            Modifier.height(2.dp)
                                    )

                                    Text(
                                        text =
                                            formatarDinheiro(
                                                restante
                                            ),

                                        fontSize = 21.sp,

                                        fontWeight =
                                            FontWeight.Bold,

                                        color =
                                            corStatusFatura
                                    )

                                } else {

                                    Text(
                                        text = "Paga",

                                        fontSize = 18.sp,

                                        fontWeight =
                                            FontWeight.Bold,

                                        color =
                                            MaterialTheme.colorScheme.primary
                                    )
                                }


                                // =========================================
                                // DATAS
                                // =========================================

                                if (
                                    fechamento != null &&
                                    vencimento != null
                                ) {

                                    Spacer(
                                        modifier =
                                            Modifier.height(14.dp)
                                    )


                                    Row(
                                        modifier =
                                            Modifier.fillMaxWidth(),

                                        horizontalArrangement =
                                            Arrangement.SpaceBetween
                                    ) {

                                        Column {

                                            Text(
                                                text = "Fechamento",

                                                fontSize = 11.sp,

                                                color =
                                                    MaterialTheme.colorScheme
                                                        .onSurfaceVariant
                                            )

                                            Text(
                                                text =
                                                    formatarDataCalendario(
                                                        fechamento
                                                    ),

                                                fontSize = 13.sp,

                                                fontWeight =
                                                    FontWeight.Medium
                                            )
                                        }


                                        Column(
                                            horizontalAlignment =
                                                androidx.compose.ui.Alignment.End
                                        ) {

                                            Text(
                                                text = "Vencimento",

                                                fontSize = 11.sp,

                                                color =
                                                    MaterialTheme.colorScheme
                                                        .onSurfaceVariant
                                            )

                                            Text(
                                                text =
                                                    formatarDataCalendario(
                                                        vencimento
                                                    ),

                                                fontSize = 13.sp,

                                                fontWeight =
                                                    FontWeight.Medium
                                            )
                                        }
                                    }
                                }


                                // =========================================
                                // CONTEÚDO EXPANDIDO
                                // =========================================

                                if (estaExpandido) {

                                    Spacer(
                                        modifier =
                                            Modifier.height(18.dp)
                                    )

                                    HorizontalDivider(
                                        color =
                                            MaterialTheme.colorScheme
                                                .outlineVariant
                                    )

                                    Spacer(
                                        modifier =
                                            Modifier.height(18.dp)
                                    )


                                    // TOTAL / PAGO
                                    Row(
                                        modifier =
                                            Modifier.fillMaxWidth(),

                                        horizontalArrangement =
                                            Arrangement.SpaceBetween
                                    ) {

                                        Column {

                                            Text(
                                                text = "Total da fatura",
                                                fontSize = 12.sp,
                                                color =
                                                    MaterialTheme.colorScheme
                                                        .onSurfaceVariant
                                            )

                                            Text(
                                                text =
                                                    formatarDinheiro(
                                                        totalFatura
                                                    ),

                                                fontSize = 15.sp,

                                                fontWeight =
                                                    FontWeight.SemiBold
                                            )
                                        }


                                        Column(
                                            horizontalAlignment =
                                                androidx.compose.ui.Alignment.End
                                        ) {

                                            Text(
                                                text = "Pago",
                                                fontSize = 12.sp,
                                                color =
                                                    MaterialTheme.colorScheme
                                                        .onSurfaceVariant
                                            )

                                            Text(
                                                text =
                                                    formatarDinheiro(
                                                        totalPago
                                                    ),

                                                fontSize = 15.sp,

                                                fontWeight =
                                                    FontWeight.SemiBold,

                                                color =
                                                    MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }


                                    // =====================================
                                    // PAGAR FATURA
                                    // =====================================

                                    if (restante > 0.01) {

                                        Spacer(
                                            modifier =
                                                Modifier.height(16.dp)
                                        )

                                        Button(
                                            onClick = {

                                                cartaoParaPagamento =
                                                    cartaoId

                                                nomeCartaoParaPagamento =
                                                    primeiraParcela.cartaoNome

                                                valorRestantePagamento =
                                                    restante

                                                valorPagamento =
                                                    String.format(
                                                        Locale.getDefault(),
                                                        "%.2f",
                                                        restante
                                                    )
                                                        .replace(
                                                            ".",
                                                            ","
                                                        )

                                                contaPagamento =
                                                    contas.firstOrNull { conta ->
                                                        conta.id == cartao?.contaId
                                                    }
                                                        ?: contas.firstOrNull()

                                                mensagemPagamento =
                                                    ""
                                            },

                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .height(50.dp),

                                            shape =
                                                androidx.compose.foundation.shape
                                                    .RoundedCornerShape(14.dp),

                                            colors =
                                                androidx.compose.material3.ButtonDefaults
                                                    .buttonColors(
                                                        containerColor =
                                                            MaterialTheme.colorScheme.primary,

                                                        contentColor =
                                                            MaterialTheme.colorScheme.onPrimary
                                                    ),

                                            elevation =
                                                androidx.compose.material3.ButtonDefaults
                                                    .buttonElevation(
                                                        defaultElevation = 0.dp,
                                                        pressedElevation = 1.dp
                                                    )
                                        ) {

                                            Text(
                                                text = "Pagar fatura",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }


                                    Spacer(
                                        modifier =
                                            Modifier.height(20.dp)
                                    )


                                    // =====================================
                                    // COMPRAS
                                    // =====================================

                                    Text(
                                        text = "Compras",

                                        fontSize = 16.sp,

                                        fontWeight =
                                            FontWeight.SemiBold
                                    )


                                    Spacer(
                                        modifier =
                                            Modifier.height(8.dp)
                                    )


                                    parcelasCartao
                                        .forEach { parcela ->

                                            Row(
                                                modifier =
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .padding(
                                                            vertical = 9.dp
                                                        ),

                                                verticalAlignment =
                                                    androidx.compose.ui.Alignment
                                                        .CenterVertically
                                            ) {

                                                Column(
                                                    modifier =
                                                        Modifier.weight(1f)
                                                ) {

                                                    Text(
                                                        text =
                                                            parcela.descricao,

                                                        fontSize = 14.sp,

                                                        fontWeight =
                                                            FontWeight.Medium,

                                                        color =
                                                            if (
                                                                parcela.quitadaAnteriormente
                                                            ) {

                                                                MaterialTheme.colorScheme
                                                                    .onSurfaceVariant

                                                            } else {

                                                                MaterialTheme.colorScheme
                                                                    .onSurface
                                                            }
                                                    )


                                                    Spacer(
                                                        modifier =
                                                            Modifier.height(2.dp)
                                                    )


                                                    Text(
                                                        text =
                                                            "${parcela.numeroParcela}/" +
                                                                    parcela.totalParcelas,

                                                        fontSize = 12.sp,

                                                        color =
                                                            MaterialTheme.colorScheme
                                                                .onSurfaceVariant
                                                    )


                                                    if (
                                                        parcela.quitadaAnteriormente
                                                    ) {

                                                        Text(
                                                            text =
                                                                "Quitada anteriormente",

                                                            fontSize = 11.sp,

                                                            color =
                                                                MaterialTheme.colorScheme
                                                                    .onSurfaceVariant
                                                        )
                                                    }
                                                }


                                                Spacer(
                                                    modifier =
                                                        Modifier.width(12.dp)
                                                )


                                                Text(
                                                    text =
                                                        formatarDinheiro(
                                                            parcela.valor
                                                        ),

                                                    fontSize = 14.sp,

                                                    fontWeight =
                                                        FontWeight.SemiBold,

                                                    color =
                                                        if (
                                                            parcela.quitadaAnteriormente
                                                        ) {

                                                            MaterialTheme.colorScheme
                                                                .onSurfaceVariant

                                                        } else {

                                                            MaterialTheme.colorScheme
                                                                .onSurface
                                                        }
                                                )
                                            }
                                        }


                                    // =====================================
                                    // PAGAMENTOS
                                    // =====================================

                                    if (
                                        pagamentosDoCartao.isNotEmpty()
                                    ) {

                                        Spacer(
                                            modifier =
                                                Modifier.height(14.dp)
                                        )

                                        HorizontalDivider(
                                            color =
                                                MaterialTheme.colorScheme
                                                    .outlineVariant
                                        )

                                        Spacer(
                                            modifier =
                                                Modifier.height(14.dp)
                                        )


                                        Text(
                                            text = "Pagamentos",

                                            fontSize = 15.sp,

                                            fontWeight =
                                                FontWeight.SemiBold
                                        )


                                        Spacer(
                                            modifier =
                                                Modifier.height(6.dp)
                                        )


                                        pagamentosDoCartao
                                            .forEach { pagamento ->

                                                Row(
                                                    modifier =
                                                        Modifier
                                                            .fillMaxWidth()
                                                            .clickable {

                                                                pagamentoParaExcluir =
                                                                    pagamento
                                                            }
                                                            .padding(
                                                                vertical = 8.dp
                                                            ),

                                                    horizontalArrangement =
                                                        Arrangement.SpaceBetween
                                                ) {

                                                    Column {

                                                        Text(
                                                            text =
                                                                pagamento.dataPagamento,

                                                            fontSize = 13.sp
                                                        )

                                                        Text(
                                                            text =
                                                                pagamento.contaNome,

                                                            fontSize = 11.sp,

                                                            color =
                                                                MaterialTheme.colorScheme
                                                                    .onSurfaceVariant
                                                        )
                                                    }


                                                    Text(
                                                        text =
                                                            formatarDinheiro(
                                                                pagamento.valorPago
                                                            ),

                                                        fontSize = 14.sp,

                                                        fontWeight =
                                                            FontWeight.Bold,

                                                        color =
                                                            MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                    }
                                }
                            }
                        }
                    }

                    item {

                        Spacer(
                            modifier =
                                Modifier.height(20.dp)
                        )
                    }
                }
            }


            item {

                Spacer(
                    modifier =
                        Modifier.height(40.dp)
                )
            }
        }
        // =========================================================
        // POPUP - PAGAR FATURA
        // =========================================================

        if (cartaoParaPagamento != null) {

            AlertDialog(
                onDismissRequest = {

                    cartaoParaPagamento =
                        null

                    mensagemPagamento =
                        ""
                },

                title = {

                    Column {

                        Text(
                            text =
                                "Pagar fatura",

                            fontWeight =
                                FontWeight.Bold
                        )

                        Spacer(
                            modifier =
                                Modifier.height(2.dp)
                        )

                        Text(
                            text =
                                nomeCartaoParaPagamento,

                            fontSize = 13.sp,

                            fontWeight =
                                FontWeight.Normal,

                            color =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant
                        )
                    }
                },

                text = {

                    Column(
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {


                        // =========================================
                        // VALOR EM ABERTO
                        // =========================================

                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color =
                                            MaterialTheme.colorScheme.tertiaryContainer,

                                        shape =
                                            androidx.compose.foundation.shape
                                                .RoundedCornerShape(
                                                    16.dp
                                                )
                                    )
                                    .padding(
                                        horizontal = 16.dp,
                                        vertical = 14.dp
                                    )
                        ) {

                            Column {

                                Text(
                                    text = "Em aberto",

                                    fontSize = 12.sp,

                                    color =
                                        MaterialTheme.colorScheme
                                            .onSurfaceVariant
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(2.dp)
                                )

                                Text(
                                    text =
                                        formatarDinheiro(
                                            valorRestantePagamento
                                        ),

                                    fontSize = 21.sp,

                                    fontWeight =
                                        FontWeight.Bold,

                                    color =
                                        MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }


                        Spacer(
                            modifier =
                                Modifier.height(18.dp)
                        )


                        // =========================================
                        // VALOR DO PAGAMENTO
                        // =========================================

                        OutlinedTextField(
                            value =
                                valorPagamento,

                            onValueChange = {

                                valorPagamento = it

                                mensagemPagamento = ""
                            },

                            label = {
                                Text(
                                    "Valor do pagamento"
                                )
                            },

                            modifier =
                                Modifier.fillMaxWidth(),

                            singleLine = true,

                            shape =
                                androidx.compose.foundation.shape
                                    .RoundedCornerShape(
                                        14.dp
                                    )
                        )


                        Spacer(
                            modifier =
                                Modifier.height(16.dp)
                        )


                        // =========================================
                        // CONTA
                        // =========================================

                        Text(
                            text = "Conta do pagamento",

                            fontSize = 13.sp,

                            fontWeight =
                                FontWeight.Medium,

                            color =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant
                        )


                        Spacer(
                            modifier =
                                Modifier.height(6.dp)
                        )


                        if (contas.isNotEmpty()) {

                            DropdownBlik(
                                valorSelecionado =
                                    contaPagamento
                                        ?.nome
                                        ?: "Selecione",

                                opcoes =
                                    contas.map { conta ->
                                        conta.nome
                                    },

                                modifier =
                                    Modifier.fillMaxWidth(),

                                onSelecionar = { nomeConta ->

                                    contaPagamento =
                                        contas.firstOrNull { conta ->
                                            conta.nome ==
                                                    nomeConta
                                        }

                                    mensagemPagamento =
                                        ""
                                }
                            )

                        } else {

                            Text(
                                text =
                                    "Nenhuma conta disponível.",

                                fontSize = 13.sp,

                                color =
                                    MaterialTheme.colorScheme.error
                            )
                        }


                        Spacer(
                            modifier =
                                Modifier.height(16.dp)
                        )


                        // =========================================
                        // DATA
                        // =========================================

                        Text(
                            text = "Data do pagamento",

                            fontSize = 13.sp,

                            fontWeight =
                                FontWeight.Medium,

                            color =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant
                        )


                        Spacer(
                            modifier =
                                Modifier.height(6.dp)
                        )


                        androidx.compose.material3.OutlinedButton(
                            onClick = {

                                mostrarCalendarioPagamento =
                                    true
                            },

                            modifier =
                                Modifier.fillMaxWidth(),

                            shape =
                                androidx.compose.foundation.shape
                                    .RoundedCornerShape(
                                        14.dp
                                    )
                        ) {

                            Text(
                                text =
                                    dataPagamento,

                                modifier =
                                    Modifier.fillMaxWidth(),

                                textAlign =
                                    TextAlign.Center
                            )
                        }


                        // =========================================
                        // ERRO
                        // =========================================

                        if (
                            mensagemPagamento
                                .isNotBlank()
                        ) {

                            Spacer(
                                modifier =
                                    Modifier.height(10.dp)
                            )


                            Text(
                                text =
                                    mensagemPagamento,

                                fontSize = 12.sp,

                                color =
                                    MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },

                confirmButton = {

                    TextButton(
                        onClick = {

                            val valorConvertido =
                                valorPagamento
                                    .replace(".", "")
                                    .replace(",", ".")
                                    .toDoubleOrNull()

                            val cartaoId =
                                cartaoParaPagamento

                            val conta =
                                contaPagamento


                            if (
                                valorConvertido == null ||
                                valorConvertido <= 0
                            ) {

                                mensagemPagamento =
                                    "Digite um valor válido."

                            } else if (
                                valorConvertido >
                                valorRestantePagamento
                            ) {

                                mensagemPagamento =
                                    "O pagamento não pode ser maior que o valor restante."

                            } else if (
                                conta == null
                            ) {

                                mensagemPagamento =
                                    "Selecione uma conta."

                            } else if (
                                cartaoId != null
                            ) {

                                onPagar(
                                    cartaoId,
                                    conta.id,
                                    mesSelecionado,
                                    anoSelecionado,
                                    valorConvertido,
                                    dataPagamento
                                )

                                cartaoParaPagamento =
                                    null

                                mensagemPagamento =
                                    ""
                            }
                        }
                    ) {

                        Text("Confirmar")
                    }
                },

                dismissButton = {

                    TextButton(
                        onClick = {

                            cartaoParaPagamento =
                                null

                            mensagemPagamento =
                                ""
                        }
                    ) {

                        Text("Cancelar")
                    }
                }
            )
        }

        pagamentoParaExcluir?.let { pagamento ->

            AlertDialog(
                onDismissRequest = {
                    pagamentoParaExcluir = null
                },

                title = {

                    Column {

                        Text(
                            text = "Excluir pagamento?",
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(2.dp)
                        )

                        Text(
                            text = "Revise os dados antes de continuar",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant
                        )
                    }
                },

                text = {

                    Column(
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {


                        // =====================================
                        // VALOR
                        // =====================================

                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color =
                                            MaterialTheme.colorScheme.errorContainer,

                                        shape =
                                            androidx.compose.foundation.shape
                                                .RoundedCornerShape(
                                                    16.dp
                                                )
                                    )
                                    .padding(
                                        horizontal = 16.dp,
                                        vertical = 14.dp
                                    )
                        ) {

                            Column {

                                Text(
                                    text = "Pagamento",

                                    fontSize = 12.sp,

                                    color =
                                        MaterialTheme.colorScheme
                                            .onSurfaceVariant
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(2.dp)
                                )

                                Text(
                                    text =
                                        formatarDinheiro(
                                            pagamento.valorPago
                                        ),

                                    fontSize = 21.sp,

                                    fontWeight =
                                        FontWeight.Bold,

                                    color =
                                        MaterialTheme.colorScheme.error
                                )
                            }
                        }


                        Spacer(
                            modifier =
                                Modifier.height(18.dp)
                        )


                        DetalheMovimentacao(
                            titulo = "Conta",
                            valor = pagamento.contaNome
                        )


                        DetalheMovimentacao(
                            titulo = "Data",
                            valor = pagamento.dataPagamento
                        )


                        Spacer(
                            modifier =
                                Modifier.height(16.dp)
                        )


                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color =
                                            MaterialTheme.colorScheme
                                                .surfaceVariant,

                                        shape =
                                            androidx.compose.foundation.shape
                                                .RoundedCornerShape(
                                                    14.dp
                                                )
                                    )
                                    .padding(
                                        12.dp
                                    )
                        ) {

                            Text(
                                text =
                                    "Ao excluir este pagamento, " +
                                            "o valor voltará a ficar em aberto na fatura.",

                                fontSize = 13.sp,

                                color =
                                    MaterialTheme.colorScheme
                                        .onSurfaceVariant
                            )
                        }
                    }
                },

                confirmButton = {

                    TextButton(
                        onClick = {

                            onExcluirPagamento(
                                pagamento
                            )

                            pagamentoParaExcluir =
                                null
                        }
                    ) {

                        Text(
                            text = "Excluir",
                            color =
                                MaterialTheme.colorScheme.error,

                            fontWeight =
                                FontWeight.SemiBold
                        )
                    }
                },

                dismissButton = {

                    TextButton(
                        onClick = {
                            pagamentoParaExcluir =
                                null
                        }
                    ) {

                        Text("Cancelar")
                    }
                }
            )
        }


        // =========================================================
        // CALENDÁRIO - PAGAMENTO
        // =========================================================

        if (mostrarCalendarioPagamento) {

            val datePickerState =
                rememberDatePickerState()

            DatePickerDialog(
                onDismissRequest = {
                    mostrarCalendarioPagamento =
                        false
                },

                confirmButton = {

                    TextButton(
                        onClick = {

                            datePickerState
                                .selectedDateMillis
                                ?.let { millis ->

                                    val calendarioUtc =
                                        java.util.Calendar
                                            .getInstance(
                                                java.util.TimeZone
                                                    .getTimeZone(
                                                        "UTC"
                                                    )
                                            )

                                    calendarioUtc
                                        .timeInMillis =
                                        millis


                                    val dia =
                                        calendarioUtc.get(
                                            java.util.Calendar
                                                .DAY_OF_MONTH
                                        )

                                    val mes =
                                        calendarioUtc.get(
                                            java.util.Calendar
                                                .MONTH
                                        ) + 1

                                    val ano =
                                        calendarioUtc.get(
                                            java.util.Calendar
                                                .YEAR
                                        )


                                    dataPagamento =
                                        String.format(
                                            Locale.getDefault(),
                                            "%02d/%02d/%04d",
                                            dia,
                                            mes,
                                            ano
                                        )
                                }

                            mostrarCalendarioPagamento =
                                false
                        }
                    ) {

                        Text("OK")
                    }
                },

                dismissButton = {

                    TextButton(
                        onClick = {
                            mostrarCalendarioPagamento =
                                false
                        }
                    ) {

                        Text("Cancelar")
                    }
                }
            ) {

                DatePicker(
                    state = datePickerState
                )
            }
        }
    }
}

fun gerarParcelasCartao(
    movimentacaoId: Int,
    cartaoId: Int,
    valorTotal: Double,
    quantidadeParcelas: Int,
    dataCompra: String,
    diaFechamento: Int,
    diaVencimento: Int,

    // null = movimentação nova:
    // calcula automaticamente o histórico.
    //
    // Set, mesmo vazio = edição:
    // preserva exatamente o histórico já existente.
    quitadasAnteriormentePreservadas: Set<Int>? = null

): List<ParcelaCartaoEntity> {

    val partes =
        dataCompra.split("/")

    if (partes.size != 3) {
        return emptyList()
    }


    val diaCompra =
        partes[0].toIntOrNull()
            ?: return emptyList()

    val mesCompra =
        partes[1].toIntOrNull()
            ?: return emptyList()

    val anoCompra =
        partes[2].toIntOrNull()
            ?: return emptyList()


    // =====================================================
    // PRIMEIRA FATURA
    // =====================================================

    var mesPrimeiraFatura =
        mesCompra

    var anoPrimeiraFatura =
        anoCompra


    if (diaCompra > diaFechamento) {

        mesPrimeiraFatura++

        if (mesPrimeiraFatura > 12) {

            mesPrimeiraFatura = 1
            anoPrimeiraFatura++
        }
    }


    // =====================================================
    // DATA ATUAL SEM HORÁRIO
    // =====================================================

    val hoje =
        java.util.Calendar
            .getInstance()
            .apply {

                set(
                    java.util.Calendar.HOUR_OF_DAY,
                    0
                )

                set(
                    java.util.Calendar.MINUTE,
                    0
                )

                set(
                    java.util.Calendar.SECOND,
                    0
                )

                set(
                    java.util.Calendar.MILLISECOND,
                    0
                )
            }


    // =====================================================
    // VALOR DAS PARCELAS EM CENTAVOS
    // =====================================================

    val valorTotalCentavos =
        kotlin.math.round(
            valorTotal * 100
        ).toLong()


    val valorBaseCentavos =
        valorTotalCentavos /
                quantidadeParcelas


    val restoCentavos =
        valorTotalCentavos %
                quantidadeParcelas


    // =====================================================
    // GERAR PARCELAS
    // =====================================================

    return (1..quantidadeParcelas)
        .map { numero ->


            // Descobre mês/ano da parcela
            val calendarioFatura =
                java.util.Calendar
                    .getInstance()
                    .apply {

                        clear()

                        set(
                            java.util.Calendar.YEAR,
                            anoPrimeiraFatura
                        )

                        set(
                            java.util.Calendar.MONTH,
                            mesPrimeiraFatura - 1
                        )

                        set(
                            java.util.Calendar.DAY_OF_MONTH,
                            1
                        )

                        add(
                            java.util.Calendar.MONTH,
                            numero - 1
                        )
                    }


            val mesFatura =
                calendarioFatura.get(
                    java.util.Calendar.MONTH
                ) + 1


            val anoFatura =
                calendarioFatura.get(
                    java.util.Calendar.YEAR
                )


            // =============================================
            // VENCIMENTO DA FATURA
            // =============================================

            val vencimento =
                calcularVencimentoFatura(
                    mesFatura =
                        mesFatura,

                    anoFatura =
                        anoFatura,

                    diaFechamento =
                        diaFechamento,

                    diaVencimento =
                        diaVencimento
                )


            // =============================================
            // HISTÓRICO
            // =============================================

            val quitadaAnteriormente =
                if (
                    quitadasAnteriormentePreservadas != null
                ) {

                    // EDIÇÃO:
                    // não recalcula com base na data de hoje.
                    numero in
                            quitadasAnteriormentePreservadas

                } else {

                    // NOVA MOVIMENTAÇÃO:
                    // se o vencimento daquela parcela já passou,
                    // consideramos que ela foi quitada antes
                    // do controle pelo Blik.
                    vencimento.before(
                        hoje
                    )
                }


            // =============================================
            // VALOR
            // =============================================

            val valorParcelaCentavos =
                if (
                    numero ==
                    quantidadeParcelas
                ) {

                    valorBaseCentavos +
                            restoCentavos

                } else {

                    valorBaseCentavos
                }


            ParcelaCartaoEntity(
                movimentacaoId =
                    movimentacaoId,

                cartaoId =
                    cartaoId,

                numeroParcela =
                    numero,

                totalParcelas =
                    quantidadeParcelas,

                valor =
                    valorParcelaCentavos /
                            100.0,

                mesFatura =
                    mesFatura,

                anoFatura =
                    anoFatura,

                quitadaAnteriormente =
                    quitadaAnteriormente
            )
        }
}

fun formatarDinheiro(valor: Double): String {
    val formato =
        java.text.NumberFormat
            .getCurrencyInstance(
                java.util.Locale(
                    "pt",
                    "BR"
                )
            )
    return formato.format(valor)
}
fun criarDataFatura(
    dia: Int,
    mes: Int,
    ano: Int
): java.util.Calendar {

    val calendario = java.util.Calendar.getInstance()
    calendario.clear()
    calendario.set( java.util.Calendar.YEAR, ano)
    calendario.set( java.util.Calendar.MONTH, mes - 1)

    val ultimoDiaDoMes = calendario.getActualMaximum( java.util.Calendar.DAY_OF_MONTH)
    calendario.set( java.util.Calendar.DAY_OF_MONTH, dia.coerceAtMost(ultimoDiaDoMes))

    return calendario

}

fun calcularVencimentoFatura(
    mesFatura: Int,
    anoFatura: Int,
    diaFechamento: Int,
    diaVencimento: Int
): java.util.Calendar {
    var mesVencimento = mesFatura
    var anoVencimento = anoFatura

    if (diaVencimento <= diaFechamento) {
        mesVencimento++
        if (mesVencimento > 12) {
            mesVencimento = 1
            anoVencimento++
        }
    }

    return criarDataFatura(
        dia = diaVencimento,
        mes = mesVencimento,
        ano = anoVencimento
    )
}

fun formatarDataCalendario(
    calendario: java.util.Calendar
): String {
    return String.format(
        Locale.getDefault(),
        "%02d/%02d/%04d",

        calendario.get(
            java.util.Calendar.DAY_OF_MONTH
        ),
        calendario.get(
            java.util.Calendar.MONTH
        ) + 1,
        calendario.get(
            java.util.Calendar.YEAR
        )
    )
}

fun calcularStatusFatura(
    restante: Double,
    mesFatura: Int,
    anoFatura: Int,
    diaFechamento: Int,
    diaVencimento: Int
): String {
    if (restante < 0.01) {
        return "Paga"
    }

    val hoje = java.util.Calendar.getInstance()
    hoje.set(java.util.Calendar.HOUR_OF_DAY, 0)
    hoje.set(java.util.Calendar.MINUTE, 0)
    hoje.set(java.util.Calendar.SECOND, 0)
    hoje.set(java.util.Calendar.MILLISECOND, 0)

    val fechamento =
        criarDataFatura(
            dia = diaFechamento,
            mes = mesFatura,
            ano = anoFatura
        )

    val vencimento =
        calcularVencimentoFatura(
            mesFatura = mesFatura,
            anoFatura = anoFatura,
            diaFechamento = diaFechamento,
            diaVencimento = diaVencimento
        )
    return when {
        hoje.after(vencimento) -> "Vencida"
        hoje.before(fechamento) -> "Aberta"
        else -> "Fechada"
    }
}

@Composable
fun MarcaBlik(
    modifier: Modifier = Modifier
) {

    Image(
        painter =
            painterResource(
                id = R.drawable.logo_blik
            ),

        contentDescription =
            "Logo Blik",

        modifier =
            modifier
                .width(90.dp)
                .height(36.dp),

        contentScale =
            androidx.compose.ui.layout.ContentScale.Fit
    )
}


