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
    val contaId: Int,
    val contaNome: String,
    val categoriaId: Int,
    val categoriaNome: String,
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

    val movimentacoes = movimentacoesEntity.map { item ->

        Movimentacao(
            id = item.id,
            descricao = item.descricao,
            valor = item.valor,
            tipo = item.tipo,
            contaId = item.contaId,
            contaNome = item.contaNome,
            categoriaId = item.categoriaId,
            categoriaNome = item.categoriaNome,
            data = item.data
        )
    }

    when (telaAtual) {

        "inicio" -> {
            TelaInicial(
                onNovaMovimentacao = {
                    telaAtual = "nova_movimentacao"
                },
                onContas = {
                    telaAtual = "contas"
                },
                onCategorias = {
                    telaAtual = "categorias"
                },
                onHistorico = {
                    telaAtual = "historico"
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

                onSalvar = { novaMovimentacao ->

                    scope.launch {
                        dao.inserir(
                            MovimentacaoEntity(
                                descricao = novaMovimentacao.descricao,
                                valor = novaMovimentacao.valor,
                                tipo = novaMovimentacao.tipo,
                                contaId = novaMovimentacao.contaId,
                                categoriaId = novaMovimentacao.categoriaId,
                                data = novaMovimentacao.data
                            )
                        )
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
                movimentacaoParaEditar = movimentacaoEmEdicao,

                onSalvar = { movimentacao ->

                    scope.launch {

                        dao.editar(
                            id = movimentacao.id,
                            descricao = movimentacao.descricao,
                            valor = movimentacao.valor,
                            tipo = movimentacao.tipo,
                            contaId = movimentacao.contaId,
                            categoriaId = movimentacao.categoriaId,
                            data = movimentacao.data
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

                onAdicionarConta = { nome, resultado ->
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
                                    nome = nomeLimpo
                                )
                            )

                            resultado(true)
                        }
                    }
                },

                onEditar = { conta, novoNome, resultado ->
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
                            contaDao.editarNome(
                                id = conta.id,
                                novoNome = nomeLimpo
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
    onNovaMovimentacao: () -> Unit,
    onContas: () -> Unit,
    onCategorias: () -> Unit,
    onHistorico: () -> Unit
) {

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
                            text = "R$ 3.450,00",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {

                    Column {

                        Text("Entradas")

                        Text(
                            text = "R$ 5.200,00",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column {

                        Text("Saídas")

                        Text(
                            text = "R$ 1.750,00",
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

                ContaItem(
                    nome = "Banco do Brasil",
                    saldo = "R$ 1.200,00"
                )

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

@Composable
fun TelaNovaMovimentacao(
    contas: List<ContaEntity>,
    categorias: List<CategoriaEntity>,
    movimentacaoParaEditar: Movimentacao? = null,
    onSalvar: (Movimentacao) -> Unit,
    onVoltar: () -> Unit
) {

    var tipo by remember {
        mutableStateOf(
            movimentacaoParaEditar?.tipo ?: "Saída"
        )
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

    var contaSelecionada by remember {
        mutableStateOf<ContaEntity?>(null)
    }

    var categoriaSelecionada by remember {
        mutableStateOf<CategoriaEntity?>(null)
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

    LaunchedEffect(contas, movimentacaoParaEditar) {

        if (contas.isNotEmpty()) {

            contaSelecionada =
                if (movimentacaoParaEditar != null) {

                    contas.find {
                        it.id == movimentacaoParaEditar.contaId
                    } ?: contas.first()

                } else {

                    contas.first()
                }
        }
    }

    LaunchedEffect(categorias, movimentacaoParaEditar) {

        if (categorias.isNotEmpty()) {

            categoriaSelecionada =
                if (movimentacaoParaEditar != null) {

                    categorias.find {
                        it.id == movimentacaoParaEditar.categoriaId
                    } ?: categorias.first()

                } else {

                    categorias.first()
                }
        }
    }


    var mostrarCalendario by remember {
        mutableStateOf(false)
    }

    var menuContaAberto by remember {
        mutableStateOf(false)
    }

    var menuCategoriaAberto by remember {
        mutableStateOf(false)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(20.dp)
                .fillMaxSize()
        ) {

            Text(
                text =
                    if (movimentacaoParaEditar == null) {
                        "Nova Movimentação"
                    } else {
                        "Editar Movimentação"
                    },
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Text(
                text = "Tipo",
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Button(
                    onClick = {
                        tipo = "Entrada"
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Entrada")
                }

                Button(
                    onClick = {
                        tipo = "Saída"
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Saída")
                }
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            OutlinedTextField(
                value = descricao,
                onValueChange = {
                    descricao = it
                },
                label = {
                    Text("Descrição")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            OutlinedTextField(
                value = valor,
                onValueChange = {
                    valor = it
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

            Button(
                onClick = {
                    mostrarCalendario = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Data: $data")
            }

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

                    contas.forEach { contaBanco ->

                        DropdownMenuItem(
                            text = {
                                Text(contaBanco.nome)
                            },
                            onClick = {
                                contaSelecionada = contaBanco
                                menuContaAberto = false
                            }
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {

                Button(
                    onClick = {
                        menuCategoriaAberto = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Categoria: ${categoriaSelecionada?.nome ?: "Selecione"}"
                    )
                }

                DropdownMenu(
                    expanded = menuCategoriaAberto,
                    onDismissRequest = {
                        menuCategoriaAberto = false
                    }
                ) {


                    categorias.forEach { categoriaBanco ->

                        DropdownMenuItem(
                            text = {
                                Text(categoriaBanco.nome)
                            },
                            onClick = {
                                categoriaSelecionada = categoriaBanco
                                menuCategoriaAberto = false
                            }
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Text(
                text = "Tipo selecionado: $tipo"
            )

            Text(
                text = "Conta: ${contaSelecionada?.nome ?: "Nenhuma"}"
            )

            Text(
                text = "Categoria: ${categoriaSelecionada?.nome ?: "Nenhuma"}"
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Button(
                onClick = {

                    val valorConvertido =
                        valor
                            .replace(",", ".")
                            .toDoubleOrNull()

                    val contaEscolhida = contaSelecionada
                    val categoriaEscolhida = categoriaSelecionada

                    if (
                        descricao.isNotBlank() &&
                        valorConvertido != null &&
                        contaEscolhida != null &&
                        categoriaEscolhida != null
                    ) {

                        val movimentacao = Movimentacao(
                            id = movimentacaoParaEditar?.id ?: 0,
                            descricao = descricao,
                            valor = valorConvertido,
                            tipo = tipo,
                            contaId = contaEscolhida.id,
                            contaNome = contaEscolhida.nome,
                            categoriaId = categoriaEscolhida.id,
                            categoriaNome = categoriaEscolhida.nome,
                            data = data
                        )

                        onSalvar(movimentacao)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salvar")
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Button(
                onClick = onVoltar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Voltar")
            }
        }

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
        String, (Boolean) -> Unit
    ) -> Unit,
    onEditar: (
        ContaEntity,
        String,
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

    var contaParaEditar by remember {
        mutableStateOf<ContaEntity?>(null)
    }
    var novoNomeConta by remember {
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

                Button(
                    onClick = {

                        if (nomeNovaConta.isNotBlank()) {

                            onAdicionarConta(
                                nomeNovaConta
                            ) { sucesso ->

                                if (sucesso) {

                                    mensagem =
                                        "Conta adicionada."

                                    nomeNovaConta = ""

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

                        if (novoNomeConta.isBlank()) {

                            mensagemEdicao =
                                "O nome da conta não pode ficar vazio."

                        } else {

                            onEditar(
                                conta,
                                novoNomeConta
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

    val contasDisponiveis =
        listOf("Todas") +
                movimentacoes
                    .map { it.contaNome }
                    .distinct()
                    .sorted()

    val categoriasDisponiveis =
        listOf("Todas") +
                movimentacoes
                    .map { it.categoriaNome }
                    .distinct()
                    .sorted()

    val tipos = listOf(
        "Todos",
        "Entrada",
        "Saída"
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
                            movimentacao.contaNome == contaSelecionada

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
                                        if (
                                            movimentacao.tipo == "Entrada"
                                        ) {
                                            "+ R$ %.2f".format(
                                                movimentacao.valor
                                            )
                                        } else {
                                            "- R$ %.2f".format(
                                                movimentacao.valor
                                            )
                                        },
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(
                                modifier = Modifier.height(4.dp)
                            )

                            Text(
                                text =
                                    "${movimentacao.contaNome} • " +
                                            movimentacao.categoriaNome
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
                                if (movimentacao.tipo == "Entrada") {
                                    "Entrada: R$ %.2f".format(
                                        movimentacao.valor
                                    )
                                } else {
                                    "Saída: R$ %.2f".format(
                                        movimentacao.valor
                                    )
                                }
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text = "Conta: ${movimentacao.contaNome}"
                        )

                        Text(
                            text = "Categoria: ${movimentacao.categoriaNome}"
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