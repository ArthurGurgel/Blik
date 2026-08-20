package com.example.blik

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.room.Room
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.blik.ui.theme.BlikTheme
import kotlinx.coroutines.launch

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

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BlikTheme {
                AppFinanceiro()
            }
        }
    }
}

@Composable
fun AppFinanceiro() {

    val context = LocalContext.current

    val banco = remember {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "blik.db"
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    val dao = banco.movimentacaoDao()
    val contaDao = banco.contaDao()
    val categoriaDao = banco.categoriaDao()
    val cartaoDao = banco.cartaoDao()

    val parcelaCartaoDao =
        banco.parcelaCartaoDao()

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

    LaunchedEffect(Unit) {
        if (contaDao.quantidade() == 0) {
            contaDao.inserirTodas(
                listOf(
                    ContaEntity(nome = "Banco do Brasil")
                )
            )
        }

        if (categoriaDao.quantidade() == 0) {

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
    }



    val scope = rememberCoroutineScope()

    var telaAtual by remember {
        mutableStateOf("inicio")
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
                onHistorico = {
                    telaAtual = "historico"
                }
            )
        }

        "cartoes" -> {
            TelaCartoes(
                cartoes = cartoes,
                contas = contas,

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
                            cartaoDao.inserir(
                                CartaoEntity(
                                    nome = nomeLimpo,
                                    limite = limite,
                                    diaFechamento = diaFechamento,
                                    diaVencimento = diaVencimento,
                                    contaId = contaId
                                )
                            )

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
                        cartaoDao.editar(
                            id = cartao.id,
                            nome = nomeLimpo,
                            limite = novoLimite,
                            diaFechamento = novoDiaFechamento,
                            diaVencimento = novoDiaVencimento,
                            contaId = novaContaId
                        )
                        resultado(true)
                    }
                }
            },
                onExcluir = { cartao ->
                    scope.launch {
                        cartaoDao.excluir(
                            cartao.id
                        )
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
                            categoriaDao.inserir(
                                CategoriaEntity(
                                    nome = nomeLimpo
                                )
                            )
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

                            categoriaDao.excluir(
                                categoria.id
                            )

                            resultado(
                                true,
                                0
                            )
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

                        val idMovimentacao =
                            dao.inserir(
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
                            )
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
                                            cartao.diaFechamento
                                    )

                                parcelaCartaoDao
                                    .inserirTodas(parcelas)
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

                        dao.editar(
                            id = movimentacao.id,
                            descricao = movimentacao.descricao,
                            valor = movimentacao.valor,
                            tipo = movimentacao.tipo,

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

                        movimentacaoEmEdicao = null
                        telaAtual = "historico"
                    }
                },

                onVoltar = {
                    movimentacaoEmEdicao = null
                    telaAtual = "historico"
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
                            contaDao.inserir(
                                ContaEntity(
                                    nome = nomeLimpo,
                                    saldoInicial = saldoInicial
                                )
                            )

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

                            resultado(true)
                        }
                    }
                },

                onDesativar = { conta->
                    scope.launch {
                        contaDao.desativar(conta.id)
                    }
                },

                onReativar = { conta ->
                    scope.launch {
                        contaDao.reativar(conta.id)
                    }
                },

                onExcluir = { conta, resultado ->
                    scope.launch {
                        val quantidadeMovimentacoes =
                            dao.quantidadePorConta(conta.id)

                        if (quantidadeMovimentacoes > 0){
                            resultado(
                                false,
                                quantidadeMovimentacoes
                            )
                        } else {
                            contaDao.excluir(conta.id)

                            resultado(
                                true,
                                0
                            )
                        }
                    }
                },

                onVoltar = {
                    telaAtual = "inicio"
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
                        dao.excluir(
                            movimentacao.id
                        )
                    }
                },

                onVoltar = {
                    telaAtual = "inicio"
                }
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaInicial(
    movimentacoes: List<Movimentacao>,
    contas: List<ContaEntity>,
    onNovaMovimentacao: () -> Unit,
    onContas: () -> Unit,
    onCategorias: () -> Unit,
    onHistorico: () -> Unit,
    onCartoes: () -> Unit
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
        "$nomeMesAtual de $anoAtual"

    val movimentacoesDoMes =
        movimentacoes.filter { movimentacao ->
            val partes =
                movimentacao.data.split("/")

            if (partes.size != 3) {
                false
            } else {
                val mes = partes[1]
                val ano = partes[2]

                mes == mesAtual &&
                        ano == anoAtual
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
            } .sumOf { movimentacao ->
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

            conta.saldoInicial +
                    entradas -
                    saidasDaConta -
                    transferenciasSaindo +
                    transferenciasEntrando
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

            ModalDrawerSheet {

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                Text(
                    text = "Blik",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )

                NavigationDrawerItem(
                    label = {
                        Text("Início")
                    },
                    selected = true,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                        }
                    }
                )
                NavigationDrawerItem(
                    label = {
                        Text("Movimentações")
                    },

                    selected = false,

                    onClick = {

                        scope.launch {
                            drawerState.close()
                        }

                        onHistorico()
                    }
                )

                NavigationDrawerItem(
                    label = {
                        Text("Gerenciar contas")
                    },
                    selected = false,
                    onClick = {

                        scope.launch {
                            drawerState.close()
                        }

                        onContas()
                    }
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
                    }
                )

                NavigationDrawerItem(
                    label = {
                        Text("Gerenciar categorias")
                    },
                    selected = false,
                    onClick = {

                        scope.launch {
                            drawerState.close()
                        }

                        onCategorias()
                    }
                )
            }
        }

    ) {

        Scaffold(

            topBar = {

                TopAppBar(

                    title = {
                        Text(
                            text = "Blik",
                            fontWeight = FontWeight.Bold
                        )
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
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                )
            },

            containerColor =
                MaterialTheme.colorScheme.background

        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(20.dp)
                    .fillMaxSize()
            ) {

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {

                        Text(
                            text = "Saldo atual",
                            fontSize = 16.sp
                        )

                        Text(
                            text = formatarDinheiro(saldoAtual),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Text(
                    text = periodoAtual,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {

                    Column {

                        Text("Entradas")

                        Text(
                            text = formatarDinheiro(entradasDoMes),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column {

                        Text("Saídas")

                        Text(
                            text = formatarDinheiro(saidasDoMes),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(28.dp)
                )

                Text(
                    text = "Contas",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                if (contas.isEmpty()) {
                    Text(
                        text = "Nenhuma conta cadastrada."
                    )
                } else {
                    contas.forEach { conta ->
                        val saldo: Double =
                            saldosPorConta[conta] ?: 0.0

                        ContaItem(
                            nome = conta.nome,
                            saldo = formatarDinheiro(saldo)
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                Button(
                    onClick = onNovaMovimentacao,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("+ Nova movimentação")
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
) {

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

    var mostrarSelecaoTipo by remember {
        mutableStateOf(false)
    }

    var mostrarSelecaoFormaPagamento by remember {
        mutableStateOf(false)
    }

    var mostrarSelecaoConta by remember {
        mutableStateOf(false)
    }

    var mostrarSelecaoContaDestino by remember {
        mutableStateOf(false)
    }

    var mostrarSelecaoCategoria by remember {
        mutableStateOf(false)
    }

    var mostrarSelecaoCartao by remember {
        mutableStateOf(false)
    }

    var mostrarSelecaoParcelas by remember {
        mutableStateOf(false)
    }

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
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(24.dp)
                )


                // DESCRIÇÃO

                OutlinedTextField(
                    value = descricao,
                    onValueChange = {
                        descricao = it
                        mensagem = ""
                    },
                    label = {
                        Text("Descrição")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
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
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )


                // TIPO

                Button(
                    onClick = {
                        mostrarSelecaoTipo = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text(
                        text =
                            if (tipo.isBlank()) {
                                "Tipo: Selecione"
                            } else {
                                "Tipo: $tipo"
                            }
                    )
                }


                // -------------------------
                // ENTRADA
                // -------------------------

                if (tipo == "Entrada") {

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Button(
                        onClick = {
                            mostrarSelecaoConta = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Text(
                            text =
                                "Conta: ${
                                    contaSelecionada?.nome
                                        ?: "Selecione"
                                }"
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Button(
                        onClick = {
                            mostrarSelecaoCategoria = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Text(
                            text =
                                "Categoria: ${
                                    categoriaSelecionada?.nome
                                        ?: "Selecione"
                                }"
                        )
                    }
                }


                // -------------------------
                // SAÍDA
                // -------------------------

                if (tipo == "Saída") {

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Button(
                        onClick = {
                            mostrarSelecaoFormaPagamento = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Text(
                            text =
                                if (formaPagamento.isBlank()) {
                                    "Forma de pagamento: Selecione"
                                } else {
                                    "Forma de pagamento: $formaPagamento"
                                }
                        )
                    }


                    // SAÍDA PELA CONTA

                    if (formaPagamento == "Conta") {

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Button(
                            onClick = {
                                mostrarSelecaoConta = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            Text(
                                text =
                                    "Conta: ${
                                        contaSelecionada?.nome
                                            ?: "Selecione"
                                    }"
                            )
                        }
                    }


                    // SAÍDA NO CRÉDITO

                    if (formaPagamento == "Crédito") {

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Button(
                            onClick = {
                                mostrarSelecaoCartao = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            Text(
                                text =
                                    "Cartão: ${
                                        cartaoSelecionado?.nome
                                            ?: "Selecione"
                                    }"
                            )
                        }

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Button(
                            onClick = {
                                mostrarSelecaoParcelas = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            Text(
                                text =
                                    "Parcelas: ${quantidadeParcelas}x"
                            )
                        }
                    }


                    if (formaPagamento.isNotBlank()) {

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Button(
                            onClick = {
                                mostrarSelecaoCategoria = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            Text(
                                text =
                                    "Categoria: ${
                                        categoriaSelecionada?.nome
                                            ?: "Selecione"
                                    }"
                            )
                        }
                    }
                }


                // -------------------------
                // TRANSFERÊNCIA
                // -------------------------

                if (tipo == "Transferência") {

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Button(
                        onClick = {
                            mostrarSelecaoConta = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Text(
                            text =
                                "Conta de origem: ${
                                    contaSelecionada?.nome
                                        ?: "Selecione"
                                }"
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Button(
                        onClick = {
                            mostrarSelecaoContaDestino = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Text(
                            text =
                                "Conta de destino: ${
                                    contaDestinoSelecionada?.nome
                                        ?: "Selecione"
                                }"
                        )
                    }
                }


                // DATA

                if (tipo.isNotBlank()) {

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Button(
                        onClick = {
                            mostrarCalendario = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Text(
                            text = "Data: $data"
                        )
                    }
                }


                // MENSAGEM

                if (mensagem.isNotBlank()) {

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Text(
                        text = mensagem
                    )
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

                                        data =
                                            data
                                    )

                                onSalvar(movimentacao)
                            }
                        },

                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Text(
                            text =
                                if (
                                    movimentacaoParaEditar == null
                                ) {
                                    "Salvar movimentação"
                                } else {
                                    "Salvar alterações"
                                }
                        )
                    }
                }


                // TEMPORÁRIO
                //
                // Vamos retirar esse botão quando implementarmos
                // corretamente o gesto Back do Android.

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Button(
                    onClick = onVoltar,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Voltar")
                }

                Spacer(
                    modifier = Modifier.height(24.dp)
                )
            }
        }


        // =====================================================
        // POPUP - TIPO
        // =====================================================

        if (mostrarSelecaoTipo) {

            AlertDialog(
                onDismissRequest = {
                    mostrarSelecaoTipo = false
                },

                title = {
                    Text("Selecionar tipo")
                },

                text = {

                    Column {

                        Button(
                            onClick = {

                                tipo = "Entrada"

                                formaPagamento = ""

                                cartaoSelecionado = null

                                contaDestinoSelecionada = null

                                quantidadeParcelas = "1"

                                mostrarSelecaoTipo = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Entrada")
                        }

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Button(
                            onClick = {

                                tipo = "Saída"

                                contaDestinoSelecionada = null

                                mostrarSelecaoTipo = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Saída")
                        }

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Button(
                            onClick = {

                                tipo = "Transferência"

                                formaPagamento = ""

                                categoriaSelecionada = null

                                cartaoSelecionado = null

                                quantidadeParcelas = "1"

                                mostrarSelecaoTipo = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Transferência")
                        }
                    }
                },

                confirmButton = {},

                dismissButton = {

                    TextButton(
                        onClick = {
                            mostrarSelecaoTipo = false
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }


        // =====================================================
        // POPUP - FORMA DE PAGAMENTO
        // =====================================================

        if (mostrarSelecaoFormaPagamento) {

            AlertDialog(
                onDismissRequest = {
                    mostrarSelecaoFormaPagamento = false
                },

                title = {
                    Text("Forma de pagamento")
                },

                text = {

                    Column {

                        Button(
                            onClick = {

                                formaPagamento = "Conta"

                                cartaoSelecionado = null

                                quantidadeParcelas = "1"

                                mostrarSelecaoFormaPagamento =
                                    false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Conta / Débito")
                        }

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Button(
                            onClick = {

                                formaPagamento = "Crédito"

                                contaSelecionada = null

                                mostrarSelecaoFormaPagamento =
                                    false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Crédito")
                        }
                    }
                },

                confirmButton = {},

                dismissButton = {

                    TextButton(
                        onClick = {
                            mostrarSelecaoFormaPagamento =
                                false
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }


        // =====================================================
        // POPUP - CONTA
        // =====================================================

        if (mostrarSelecaoConta) {

            AlertDialog(
                onDismissRequest = {
                    mostrarSelecaoConta = false
                },

                title = {

                    Text(
                        text =
                            if (tipo == "Transferência") {
                                "Conta de origem"
                            } else {
                                "Selecionar conta"
                            }
                    )
                },

                text = {

                    Column {

                        contas.forEach { conta ->

                            Button(
                                onClick = {
                                    contaSelecionada = conta
                                    mostrarSelecaoConta = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(conta.nome)
                            }

                            Spacer(
                                modifier = Modifier.height(6.dp)
                            )
                        }
                    }
                },

                confirmButton = {},

                dismissButton = {

                    TextButton(
                        onClick = {
                            mostrarSelecaoConta = false
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }


        // =====================================================
        // POPUP - CONTA DESTINO
        // =====================================================

        if (mostrarSelecaoContaDestino) {

            AlertDialog(
                onDismissRequest = {
                    mostrarSelecaoContaDestino = false
                },

                title = {
                    Text("Conta de destino")
                },

                text = {

                    Column {

                        contas
                            .filter {
                                it.id != contaSelecionada?.id
                            }
                            .forEach { conta ->

                                Button(
                                    onClick = {

                                        contaDestinoSelecionada =
                                            conta

                                        mostrarSelecaoContaDestino =
                                            false
                                    },
                                    modifier =
                                        Modifier.fillMaxWidth()
                                ) {
                                    Text(conta.nome)
                                }

                                Spacer(
                                    modifier =
                                        Modifier.height(6.dp)
                                )
                            }
                    }
                },

                confirmButton = {},

                dismissButton = {

                    TextButton(
                        onClick = {
                            mostrarSelecaoContaDestino =
                                false
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }


        // =====================================================
        // POPUP - CATEGORIA
        // =====================================================

        if (mostrarSelecaoCategoria) {

            AlertDialog(
                onDismissRequest = {
                    mostrarSelecaoCategoria = false
                },

                title = {
                    Text("Selecionar categoria")
                },

                text = {

                    Column {

                        categorias.forEach { categoria ->

                            Button(
                                onClick = {

                                    categoriaSelecionada =
                                        categoria

                                    mostrarSelecaoCategoria =
                                        false
                                },
                                modifier =
                                    Modifier.fillMaxWidth()
                            ) {
                                Text(categoria.nome)
                            }

                            Spacer(
                                modifier =
                                    Modifier.height(6.dp)
                            )
                        }
                    }
                },

                confirmButton = {},

                dismissButton = {

                    TextButton(
                        onClick = {
                            mostrarSelecaoCategoria = false
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }


        // =====================================================
        // POPUP - CARTÃO
        // =====================================================

        if (mostrarSelecaoCartao) {

            AlertDialog(
                onDismissRequest = {
                    mostrarSelecaoCartao = false
                },

                title = {
                    Text("Selecionar cartão")
                },

                text = {

                    Column {

                        cartoes.forEach { cartao ->

                            Button(
                                onClick = {

                                    cartaoSelecionado =
                                        cartao

                                    mostrarSelecaoCartao =
                                        false
                                },
                                modifier =
                                    Modifier.fillMaxWidth()
                            ) {
                                Text(cartao.nome)
                            }

                            Spacer(
                                modifier =
                                    Modifier.height(6.dp)
                            )
                        }
                    }
                },

                confirmButton = {},

                dismissButton = {

                    TextButton(
                        onClick = {
                            mostrarSelecaoCartao = false
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }


        // =====================================================
        // POPUP - PARCELAS
        // =====================================================

        if (mostrarSelecaoParcelas) {

            AlertDialog(
                onDismissRequest = {
                    mostrarSelecaoParcelas = false
                },

                title = {
                    Text("Número de parcelas")
                },

                text = {

                    LazyColumn {

                        items(
                            items = (1..24).toList()
                        ) { numero ->

                            Button(
                                onClick = {

                                    quantidadeParcelas =
                                        numero.toString()

                                    mostrarSelecaoParcelas =
                                        false
                                },
                                modifier =
                                    Modifier.fillMaxWidth()
                            ) {

                                Text(
                                    text =
                                        if (numero == 1) {
                                            "1x - À vista"
                                        } else {
                                            "${numero}x"
                                        }
                                )
                            }

                            Spacer(
                                modifier =
                                    Modifier.height(6.dp)
                            )
                        }
                    }
                },

                confirmButton = {},

                dismissButton = {

                    TextButton(
                        onClick = {
                            mostrarSelecaoParcelas = false
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
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

                                    val formato =
                                        SimpleDateFormat(
                                            "dd/MM/yyyy",
                                            Locale.getDefault()
                                        )

                                    data =
                                        formato.format(
                                            Date(millis)
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
    saldo: String
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = nome,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = saldo,
                fontWeight = FontWeight.Bold
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
            ContaEntity,(Boolean, Int) -> Unit
            ) -> Unit,
    onVoltar: () -> Unit
) {

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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(20.dp)
                .fillMaxSize()
        ) {

            item {
                Text(
                    text = "Gerenciar contas",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                OutlinedTextField(
                    value = nomeNovaConta,

                    onValueChange = {
                        nomeNovaConta = it
                        mensagem = ""
                    },

                    label = {
                        Text("Nome da nova conta")
                    },

                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                OutlinedTextField(
                    value = saldoInicialNovaConta,
                    onValueChange = {
                        saldoInicialNovaConta = it
                        mensagem = ""
                    },
                    label = {
                        Text("Saldo inicial")
                    },
                    placeholder = {
                        Text("Ex.: 1500,00")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Button(
                    onClick = {
                        val saldoConvertido =
                            saldoInicialNovaConta
                                .replace(",", "")
                                .replace(",", ".")
                                .toDoubleOrNull()

                        if (nomeNovaConta.isBlank()) {
                            mensagem =
                                "Digite o nome da conta."
                        } else if (saldoConvertido == null) {
                            mensagem =
                                "Digite um saldo inicial válido."
                        } else {
                            onAdicionarConta(
                                nomeNovaConta,
                                saldoConvertido
                            ){ sucesso ->
                                if (sucesso) {
                                    mensagem =
                                        "Conta adicionada."
                                    nomeNovaConta = ""
                                    saldoInicialNovaConta = ""
                                } else {
                                    mensagem =
                                        "Já existe uma conta com esse nome."
                                }
                            }
                        }
                    },

                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Adicionar conta")
                }

                if (mensagem.isNotBlank()) {

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = mensagem
                    )
                }

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                Text(
                    text = "Contas cadastradas",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )
            }

            items(
                items = contas,
                key = { contas ->
                    contas.id
                }
            ) { conta ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.SpaceBetween
                        ) {

                            Text(
                                text = conta.nome,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text =
                                "Saldo inicial: ${formatarDinheiro(conta.saldoInicial)}"
                            )

                            Text(
                                text =
                                    if (conta.ativa) {
                                        "Ativa"
                                    } else {
                                        "Desativada"
                                    }
                            )
                        }

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Button(
                            onClick = {
                                contaParaEditar = conta
                                novoNomeConta = conta.nome
                                novoSaldoInicial =
                                    conta.saldoInicial
                                        .toString()
                                        .replace(".", ",")
                                mensagemEdicao = ""
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Editar")
                        }

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        if (conta.ativa) {

                            Button(
                                onClick = {
                                    onDesativar(conta)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Desativar")
                            }

                        } else {

                            Button(
                                onClick = {
                                    onReativar(conta)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Reativar")
                            }
                        }

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Button(
                            onClick = {
                                contaParaExcluir = conta
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Excluir definitivamente")
                        }
                    }
                }
            }

            item {
                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                Button(
                    onClick = onVoltar,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Voltar")
                    }
                }
            }
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
                        onExcluir(conta) { sucesso, quantidade ->
                            if (sucesso) {
                                mensagemExclusao =
                                    "Conta excluída com sucesso."
                            } else {
                                mensagemExclusao =
                                    "Não é possível excluir a conta \"${conta.nome}\". " +
                                            "Existem $quantidade movimentação(ões) vinculada(s) a ela." +
                                            "Desative a conta para impedir novos lançamentos sem perder o histórico."
                            }

                            contaParaExcluir = null
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

    onExcluir: (CartaoComConta) -> Unit,

    onVoltar: () -> Unit
) {

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
    var menuContaAberto by remember {
        mutableStateOf(false)
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
    var menuNovaContaAberto by remember {
        mutableStateOf(false)
    }
    var mensagemEdicao by remember {
        mutableStateOf("")
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
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(20.dp)
                .fillMaxSize()
        ) {

            item {

                Text(
                    text = "Gerenciar cartões",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                OutlinedTextField(
                    value = nomeCartao,
                    onValueChange = {
                        nomeCartao = it
                        mensagem = ""
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
                    value = limiteCartao,
                    onValueChange = {
                        limiteCartao = it
                        mensagem = ""
                    },
                    label = {
                        Text("Limite")
                    },
                    placeholder = {
                        Text("Ex.: 5000,00")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                OutlinedTextField(
                    value = fechamentoCartao,
                    onValueChange = {
                        fechamentoCartao = it
                        mensagem = ""
                    },
                    label = {
                        Text("Dia de fechamento")
                    },
                    placeholder = {
                        Text("Ex.: 10")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                OutlinedTextField(
                    value = vencimentoCartao,
                    onValueChange = {
                        vencimentoCartao = it
                        mensagem = ""
                    },
                    label = {
                        Text("Dia de vencimento")
                    },
                    placeholder = {
                        Text("Ex.: 17")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Button(
                        onClick = {
                            menuContaAberto = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Text(
                            text =
                                "Conta: ${contaSelecionada?.nome ?: "Selecione"}"
                        )
                    }

                    DropdownMenu(
                        expanded = menuContaAberto,
                        onDismissRequest = {
                            menuContaAberto = false
                        }
                    ) {

                        contas.forEach { conta ->

                            DropdownMenuItem(
                                text = {
                                    Text(conta.nome)
                                },
                                onClick = {
                                    contaSelecionada = conta
                                    menuContaAberto = false
                                }
                            )
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Button(
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

                        } else if (limiteConvertido == null) {

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

                        } else if (contaEscolhida == null) {

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

                                    mensagem =
                                        "Cartão cadastrado com sucesso."

                                    nomeCartao = ""
                                    limiteCartao = ""
                                    fechamentoCartao = ""
                                    vencimentoCartao = ""

                                } else {

                                    mensagem =
                                        "Já existe um cartão com esse nome."
                                }
                            }
                        }
                    },

                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text("Adicionar cartão")
                }
                if (mensagem.isNotBlank()) {

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = mensagem
                    )
                }
                Spacer(
                    modifier = Modifier.height(28.dp)
                )

                Text(
                    text = "Cartões cadastrados",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
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

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {

                            Text(
                                text = cartao.nome,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )

                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )

                            Text(
                                text = "Limite: ${formatarDinheiro(cartao.limite)}"
                            )

                            Text(
                                text = "Conta: ${cartao.contaNome}"
                            )

                            Text(
                                text = "Fecha dia ${cartao.diaFechamento}"
                            )

                            Text(
                                text = "Vence dia ${cartao.diaVencimento}"
                            )
                        }
                    }
                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )
                    Button(
                        onClick = {
                            cartaoParaEditar = cartao
                            novoNomeCartao = cartao.nome
                            novoLimiteCartao =
                                cartao.limite
                                    .toString()
                                    .replace(".",",")
                            novoFechamentoCartao =
                                cartao.diaFechamento.toString()
                            novoVencimentoCartao =
                                cartao.diaVencimento.toString()
                            novaContaCartao =
                                contas.find {
                                    it.id == cartao.contaId
                                }
                            mensagemEdicao = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Editar")
                    }

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Button(
                        onClick = {
                            cartaoParaExcluir = cartao
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Excluir")
                    }
                }
            }

            item {

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Button(
                    onClick = onVoltar,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Voltar")
                }

                Spacer(
                    modifier = Modifier.height(24.dp)
                )
            }
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
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            Button(
                                onClick = {
                                    menuNovaContaAberto = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {

                                Text(
                                    text =
                                        "Conta: ${novaContaCartao?.nome ?: "Selecione"}"
                                )
                            }

                            DropdownMenu(
                                expanded = menuNovaContaAberto,
                                onDismissRequest = {
                                    menuNovaContaAberto = false
                                }
                            ) {

                                contas.forEach { conta ->

                                    DropdownMenuItem(
                                        text = {
                                            Text(conta.nome)
                                        },
                                        onClick = {
                                            novaContaCartao = conta
                                            menuNovaContaAberto = false
                                        }
                                    )
                                }
                            }
                        }

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
                },

                title = {
                    Text("Excluir cartão?")
                },

                text = {
                    Text(
                        text =
                            "Deseja excluir o cartão \"${cartao.nome}\" definitivamente?"
                    )
                },

                confirmButton = {

                    TextButton(
                        onClick = {

                            onExcluir(cartao)

                            cartaoParaExcluir = null
                        }
                    ) {
                        Text("Excluir")
                    }
                },

                dismissButton = {

                    TextButton(
                        onClick = {
                            cartaoParaExcluir = null
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

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(20.dp)
                .fillMaxSize()
        ) {
            item {
                Text(
                    text = "Gerenciar categorias",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                OutlinedTextField(
                    value = nomeNovaCategoria,

                    onValueChange = {
                        nomeNovaCategoria = it
                        mensagem = ""
                    },

                    label = {
                        Text("Nova categoria")
                    },

                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Button(
                    onClick = {

                        if (nomeNovaCategoria.isBlank()) {

                            mensagem =
                                "Digite um nome para a categoria."

                        } else {

                            onAdicionar(
                                nomeNovaCategoria
                            ) { sucesso ->

                                if (sucesso) {

                                    mensagem =
                                        "Categoria adicionada."

                                    nomeNovaCategoria = ""

                                } else {

                                    mensagem =
                                        "Já existe uma categoria com esse nome."
                                }
                            }
                        }
                    },

                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Adicionar categoria")
                }

                if (mensagem.isNotBlank()) {

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(mensagem)
                }

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                Text(
                    text = "Categorias",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )
            }

            items(
                items = categorias,
                key = { categoria ->
                    categoria.id
                }
            ) { categoria ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = categoria.nome,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Button(
                            onClick = {

                                categoriaParaEditar =
                                    categoria

                                novoNomeCategoria =
                                    categoria.nome

                                mensagemEdicao = ""
                            },

                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Editar")
                        }

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Button(
                            onClick = {
                                categoriaParaExcluir =
                                    categoria
                            },

                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Excluir")
                        }
                    }
                }
            }

            item {
                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                Button(
                    onClick = onVoltar,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Voltar")
                }
            }
        }
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
fun TelaHistorico(
    movimentacoes: List<Movimentacao>,
    onEditar: (Movimentacao) -> Unit,
    onExcluir: (Movimentacao) -> Unit,
    onVoltar: () -> Unit
) {


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

    var menuMesAberto by remember {
        mutableStateOf(false)
    }

    var menuAnoAberto by remember {
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
        modifier = Modifier.fillMaxSize()
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

                Text(
                    text = "Histórico",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text(
                    text = "Filtrar movimentações",
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    Box(
                        modifier = Modifier.weight(1f)
                    ) {

                        Button(
                            onClick = {
                                menuMesAberto = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Mês: $mesSelecionadoNome")
                        }

                        DropdownMenu(
                            expanded = menuMesAberto,
                            onDismissRequest = {
                                menuMesAberto = false
                            }
                        ) {

                            meses.forEach { mes ->

                                DropdownMenuItem(
                                    text = {
                                        Text(mes.first)
                                    },

                                    onClick = {
                                        mesSelecionado = mes.second
                                        mesSelecionadoNome = mes.first
                                        menuMesAberto = false
                                    }
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier.weight(1f)
                    ) {

                        Button(
                            onClick = {
                                menuAnoAberto = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Ano: $anoSelecionado")
                        }

                        DropdownMenu(
                            expanded = menuAnoAberto,
                            onDismissRequest = {
                                menuAnoAberto = false
                            }
                        ) {

                            anos.forEach { ano ->

                                DropdownMenuItem(
                                    text = {
                                        Text(ano)
                                    },

                                    onClick = {
                                        anoSelecionado = ano
                                        menuAnoAberto = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Button(
                    onClick = {
                        mostrarFiltros = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {

                    val filtrosAtivos =
                        listOf(
                            contaSelecionada != "Todas",
                            categoriaSelecionada != "Todas",
                            tipoSelecionado != "Todos"
                        ).count { it }

                    Text(
                        text =
                            if (filtrosAtivos == 0) {
                                "Filtros"
                            } else {
                                "Filtros ($filtrosAtivos)"
                            }
                    )
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
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

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable {
                                movimentacaoSelecionada = movimentacao
                            }
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement =
                                    Arrangement.SpaceBetween
                            ) {

                                Text(
                                    text = movimentacao.descricao,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text =
                                       when (movimentacao.tipo){
                                           "Entrada" ->
                                               "+ ${formatarDinheiro(movimentacao.valor)}"
                                           "Saída" ->
                                               "- ${formatarDinheiro(movimentacao.valor)}"
                                           "Transferência" ->
                                               formatarDinheiro(movimentacao.valor)

                                           else ->
                                               formatarDinheiro(movimentacao.valor)
                                       },
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(
                                modifier = Modifier.height(4.dp)
                            )

                            val detalheMovimentacao =
                                when {

                                    movimentacao.tipo == "Transferência" -> {

                                        "${movimentacao.contaNome ?: "Conta"} → " +
                                                (movimentacao.contaDestinoNome ?: "Conta")
                                    }

                                    movimentacao.tipo == "Saída" &&
                                            movimentacao.formaPagamento == "Crédito" -> {

                                        val parcelas =
                                            if (movimentacao.quantidadeParcelas > 1) {
                                                " • ${movimentacao.quantidadeParcelas}x"
                                            } else {
                                                ""
                                            }

                                        "${movimentacao.cartaoNome ?: "Cartão"} • Crédito$parcelas"
                                    }

                                    movimentacao.tipo == "Saída" -> {

                                        "${movimentacao.contaNome ?: "Conta"} • " +
                                                (movimentacao.categoriaNome ?: "Sem categoria")
                                    }

                                    movimentacao.tipo == "Entrada" -> {

                                        "${movimentacao.contaNome ?: "Conta"} • " +
                                                (movimentacao.categoriaNome ?: "Sem categoria")
                                    }

                                    else -> ""
                                }
                            Text(
                                text = detalheMovimentacao
                            )

                            Spacer(
                                modifier = Modifier.height(4.dp)
                            )

                            Text(
                                text = movimentacao.data,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            item {

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Button(
                    onClick = onVoltar,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Voltar")
                }

                Spacer(
                    modifier = Modifier.height(24.dp)
                )
            }
        }
        if (mostrarFiltros) {

            var menuContaDialogAberto by remember {
                mutableStateOf(false)
            }

            var menuCategoriaDialogAberto by remember {
                mutableStateOf(false)
            }

            var menuTipoDialogAberto by remember {
                mutableStateOf(false)
            }

            AlertDialog(
                onDismissRequest = {
                    mostrarFiltros = false
                },

                title = {
                    Text("Filtros")
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

                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            Button(
                                onClick = {
                                    menuContaDialogAberto = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(contaSelecionada)
                            }

                            DropdownMenu(
                                expanded = menuContaDialogAberto,
                                onDismissRequest = {
                                    menuContaDialogAberto = false
                                }
                            ) {

                                contasDisponiveis.forEach { conta ->

                                    DropdownMenuItem(
                                        text = {
                                            Text(conta)
                                        },

                                        onClick = {
                                            contaSelecionada = conta
                                            menuContaDialogAberto = false
                                        }
                                    )
                                }
                            }
                        }

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

                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            Button(
                                onClick = {
                                    menuCategoriaDialogAberto = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(categoriaSelecionada)
                            }

                            DropdownMenu(
                                expanded = menuCategoriaDialogAberto,
                                onDismissRequest = {
                                    menuCategoriaDialogAberto = false
                                }
                            ) {

                                categoriasDisponiveis.forEach { categoria ->

                                    DropdownMenuItem(
                                        text = {
                                            Text(categoria)
                                        },

                                        onClick = {
                                            categoriaSelecionada = categoria
                                            menuCategoriaDialogAberto = false
                                        }
                                    )
                                }
                            }
                        }

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

                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            Button(
                                onClick = {
                                    menuTipoDialogAberto = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(tipoSelecionado)
                            }

                            DropdownMenu(
                                expanded = menuTipoDialogAberto,
                                onDismissRequest = {
                                    menuTipoDialogAberto = false
                                }
                            ) {

                                tipos.forEach { tipo ->

                                    DropdownMenuItem(
                                        text = {
                                            Text(tipo)
                                        },

                                        onClick = {
                                            tipoSelecionado = tipo
                                            menuTipoDialogAberto = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        TextButton(
                            onClick = {

                                contaSelecionada = "Todas"
                                categoriaSelecionada = "Todas"
                                tipoSelecionado = "Todos"
                            }
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

            AlertDialog(
                onDismissRequest = {
                    movimentacaoSelecionada = null
                },

                title = {
                    Text(movimentacao.descricao)
                },

                text = {

                    Column {

                        Text(
                            text =
                                when (movimentacao.tipo) {

                                    "Entrada" ->
                                        "Entrada: ${formatarDinheiro(movimentacao.valor)}"

                                    "Saída" ->
                                        "Saída: ${formatarDinheiro(movimentacao.valor)}"

                                    "Transferência" ->
                                        "Transferência: ${formatarDinheiro(movimentacao.valor)}"

                                    else ->
                                        formatarDinheiro(movimentacao.valor)
                                }
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )


                        when {

                            movimentacao.tipo == "Transferência" -> {

                                Text(
                                    text =
                                        "Origem: ${
                                            movimentacao.contaNome
                                                ?: "Não informada"
                                        }"
                                )

                                Text(
                                    text =
                                        "Destino: ${
                                            movimentacao.contaDestinoNome
                                                ?: "Não informado"
                                        }"
                                )
                            }


                            movimentacao.tipo == "Saída" &&
                                    movimentacao.formaPagamento == "Crédito" -> {

                                Text(
                                    text =
                                        "Forma de pagamento: Crédito"
                                )

                                Text(
                                    text =
                                        "Cartão: ${
                                            movimentacao.cartaoNome
                                                ?: "Não informado"
                                        }"
                                )

                                Text(
                                    text =
                                        "Parcelas: ${
                                            movimentacao.quantidadeParcelas
                                        }x"
                                )

                                Text(
                                    text =
                                        "Categoria: ${
                                            movimentacao.categoriaNome
                                                ?: "Não informada"
                                        }"
                                )
                            }


                            else -> {

                                Text(
                                    text =
                                        "Conta: ${
                                            movimentacao.contaNome
                                                ?: "Não informada"
                                        }"
                                )

                                Text(
                                    text =
                                        "Categoria: ${
                                            movimentacao.categoriaNome
                                                ?: "Não informada"
                                        }"
                                )

                                if (
                                    movimentacao.tipo == "Saída"
                                ) {

                                    Text(
                                        text =
                                            "Forma de pagamento: ${
                                                movimentacao.formaPagamento
                                                    ?: "Não informada"
                                            }"
                                    )
                                }
                            }
                        }


                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text = "Data: ${movimentacao.data}"
                        )
                    }
                },

                confirmButton = {

                    Row {

                        TextButton(
                            onClick = {

                                movimentacaoSelecionada = null

                                onEditar(movimentacao)
                            }
                        ) {
                            Text("Editar")
                        }

                        TextButton(
                            onClick = {

                                movimentacaoParaExcluir =
                                    movimentacao

                                movimentacaoSelecionada =
                                    null
                            }
                        ) {
                            Text("Excluir")
                        }
                    }
                },

                dismissButton = {

                    TextButton(
                        onClick = {
                            movimentacaoSelecionada = null
                        }
                    ) {
                        Text("Fechar")
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
fun gerarParcelasCartao(
    movimentacaoId: Int,
    cartaoId: Int,
    valorTotal: Double,
    quantidadeParcelas: Int,
    dataCompra: String,
    diaFechamento: Int
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


    var mesPrimeiraFatura =
        mesCompra

    var anoPrimeiraFatura =
        anoCompra


    // Compra depois do fechamento:
    // vai para a fatura seguinte.

    if (diaCompra > diaFechamento) {

        mesPrimeiraFatura++

        if (mesPrimeiraFatura > 12) {

            mesPrimeiraFatura = 1
            anoPrimeiraFatura++
        }
    }


    val valorBase =
        valorTotal / quantidadeParcelas


    return (1..quantidadeParcelas).map { numero ->

        var mes =
            mesPrimeiraFatura + numero - 1

        var ano =
            anoPrimeiraFatura


        while (mes > 12) {
            mes -= 12
            ano++
        }


        ParcelaCartaoEntity(
            movimentacaoId = movimentacaoId,
            cartaoId = cartaoId,
            numeroParcela = numero,
            totalParcelas = quantidadeParcelas,
            valor = valorBase,
            mesFatura = mes,
            anoFatura = ano
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