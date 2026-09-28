package com.example.gamequest.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.gamequest.data.repository.ValidacionQrResult
import com.example.gamequest.ui.common.CampusBottomBar
import com.example.gamequest.ui.navigation.Routes
import com.example.gamequest.ui.theme.AmberAccent
import com.example.gamequest.ui.theme.AmberAccentDark
import com.example.gamequest.ui.theme.PixelCream
import com.example.gamequest.ui.theme.PixelInkOnCream
import com.example.gamequest.util.QrAnalyzer
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel,
    onNavigateTab: (String) -> Unit,
    onMisionCompletada: (misionId: Int) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var permisoConcedido by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcherPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        permisoConcedido = concedido
    }
    LaunchedEffect(Unit) {
        if (!permisoConcedido) launcherPermiso.launch(Manifest.permission.CAMERA)
    }

    var mostrarDialogoManual by remember { mutableStateOf(false) }

    Scaffold(bottomBar = { CampusBottomBar(currentRoute = Routes.SCANNER, onNavigate = onNavigateTab) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (permisoConcedido) {
                CamaraPreview(
                    activo = uiState is ScannerUiState.Escaneando,
                    onQrDetectado = { codigo -> viewModel.validar(codigo) }
                )
                // Marco de encuadre
                Box(
                    Modifier
                        .align(Alignment.Center)
                        .size(230.dp)
                        .background(Color.Transparent)
                ) {
                    Surface(
                        color = Color.Transparent,
                        modifier = Modifier.fillMaxSize(),
                        border = androidx.compose.foundation.BorderStroke(4.dp, AmberAccent),
                        shape = RoundedCornerShape(4.dp)
                    ) {}
                }
            } else {
                Column(
                    Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Se necesita permiso de cámara para escanear el código QR del punto de interés.",
                        color = Color.White,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { launcherPermiso.launch(Manifest.permission.CAMERA) },
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = PixelInkOnCream)
                    ) {
                        Text("Conceder permiso")
                    }
                }
            }

            Row(
                Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
                Spacer(Modifier.width(4.dp))
                Column(Modifier.weight(1f)) {
                    Text("Escanear código", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Encuadra el código QR del punto de interés para validar tu llegada",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                when (val estado = uiState) {
                    is ScannerUiState.Procesando -> {
                        Surface(color = PixelCream, shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = PixelInkOnCream)
                                Spacer(Modifier.width(12.dp))
                                Text("Validando código…", color = PixelInkOnCream)
                            }
                        }
                    }
                    is ScannerUiState.Resultado -> {
                        ResultadoPanel(estado.resultado, onContinuar = {
                            when (val r = estado.resultado) {
                                is ValidacionQrResult.MisionCompletada -> onMisionCompletada(r.mision.id)
                                else -> viewModel.reiniciar()
                            }
                        })
                    }
                    ScannerUiState.Escaneando -> {
                        OutlinedButton(
                            onClick = { mostrarDialogoManual = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(4.dp),
                            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Icon(Icons.Filled.Keyboard, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Ingresar código manualmente")
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogoManual) {
        var codigoManual by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { mostrarDialogoManual = false },
            title = { Text("Ingresar código manualmente") },
            text = {
                Column {
                    Text(
                        "Si la cámara falla o el código está deteriorado, escribe el código alfanumérico del punto (RF-11).",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = codigoManual,
                        onValueChange = { codigoManual = it },
                        placeholder = { Text("CQ-BIB-001") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogoManual = false
                    viewModel.validar(codigoManual)
                }) { Text("Validar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoManual = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun ResultadoPanel(resultado: ValidacionQrResult, onContinuar: () -> Unit) {
    val (icono, color, titulo, subtitulo) = when (resultado) {
        is ValidacionQrResult.MisionCompletada -> ResultadoVisual(
            Icons.Filled.CheckCircle, Color(0xFF2E7D32),
            "${resultado.punto.nombre} · Código válido",
            "+${resultado.puntosGanados} pts · Misión completada"
        )
        ValidacionQrResult.YaCompletada -> ResultadoVisual(
            Icons.Filled.Error, AmberAccentDark,
            "Ya completaste esta misión",
            "Este código ya fue validado anteriormente."
        )
        ValidacionQrResult.CodigoNoReconocido -> ResultadoVisual(
            Icons.Filled.Close, MaterialTheme.colorScheme.error,
            "Código no reconocido",
            "Verifica el código o vuelve a intentarlo."
        )
        ValidacionQrResult.SinMisionAsociada -> ResultadoVisual(
            Icons.Filled.Error, MaterialTheme.colorScheme.error,
            "Punto sin misión disponible",
            "Este punto de interés no tiene una misión activa."
        )
    }
    Surface(color = PixelCream, shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icono, contentDescription = null, tint = color)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(titulo, fontWeight = FontWeight.Bold, color = PixelInkOnCream)
                    Text(subtitulo, style = MaterialTheme.typography.bodySmall, color = PixelInkOnCream.copy(alpha = 0.8f))
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onContinuar,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = PixelInkOnCream)
            ) {
                Text(if (resultado is ValidacionQrResult.MisionCompletada) "Ver insignia" else "Entendido")
            }
        }
    }
}

private data class ResultadoVisual(
    val icono: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color,
    val titulo: String,
    val subtitulo: String
)

@Composable
private fun CamaraPreview(activo: Boolean, onQrDetectado: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var camera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }
    val analysis = remember {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
    }

    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

    // Une la cámara al ciclo de vida UNA sola vez. Antes se desligaba y
    // volvía a ligar en cada cambio de estado (Escaneando/Procesando/...),
    // lo que competía con la navegación a otras pantallas/diálogos
    // (bloqueaba el hilo principal) y podía dejar la cámara en un estado
    // inconsistente al volver desde otra ventana.
    DisposableEffect(Unit) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            try {
                val cameraProvider = providerFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis
                )
                // Enfoque continuo explícito sobre el centro del encuadre:
                // en varios dispositivos el AF continuo por defecto de
                // CameraX no se dispara solo y la cámara queda sin enfocar.
                camera?.cameraControl?.startFocusAndMetering(
                    androidx.camera.core.FocusMeteringAction.Builder(
                        SurfaceOrientedMeteringPointFactory(1f, 1f).createPoint(0.5f, 0.5f),
                        androidx.camera.core.FocusMeteringAction.FLAG_AF
                    ).setAutoCancelDuration(3, java.util.concurrent.TimeUnit.SECONDS).build()
                )
            } catch (_: Exception) {
                // Si la cámara no está disponible (por ejemplo en un emulador sin
                // sensor virtual configurado, o porque otra app/ventana la tiene
                // tomada) se deja la vista sin analizador activo en vez de tumbar
                // la app.
            }
        }, ContextCompat.getMainExecutor(context))

        // Toca para reenfocar manualmente.
        previewView.setOnTouchListener { view, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP) {
                val point = previewView.meteringPointFactory.createPoint(event.x, event.y)
                val action = androidx.camera.core.FocusMeteringAction.Builder(point).build()
                camera?.cameraControl?.startFocusAndMetering(action)
                view.performClick()
            }
            true
        }

        onDispose {
            runCatching { ProcessCameraProvider.getInstance(context).get().unbindAll() }
            cameraExecutor.shutdown()
        }
    }

    // Solo activa/desactiva el analizador de frames al pausar el escaneo;
    // no vuelve a ligar/desligar toda la cámara.
    DisposableEffect(activo) {
        if (activo) {
            analysis.setAnalyzer(cameraExecutor, QrAnalyzer(onQrDetectado))
        } else {
            analysis.clearAnalyzer()
        }
        onDispose { }
    }
}
