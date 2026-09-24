<?php
header('Content-Type: application/json; charset=utf-8');

// 1. Configuracion del directorio de almacenamiento (debe tener permisos de escritura)
$uploadDir = __DIR__ . '/deca_storage/';

// Crear el directorio si no existe
if (!is_dir($uploadDir)) {
    mkdir($uploadDir, 0755, true);
}

// 2. Verificar que la peticion contiene el archivo
if (!isset($_FILES['pdf']) || $_FILES['pdf']['error'] !== UPLOAD_ERR_OK) {
    http_response_code(400);
    echo json_encode(['error' => 'No se recibio ningun archivo PDF valido']);
    exit;
}

$file = $_FILES['pdf'];

// 3. Validacion de seguridad: tipo y tamano
$allowedMime = ['application/pdf'];
if (!in_array($file['type'], $allowedMime)) {
    http_response_code(400);
    echo json_encode(['error' => 'Solo se permiten archivos PDF']);
    exit;
}

// Limite de 5 MB segun normativa DeCA
if ($file['size'] > 5 * 1024 * 1024) {
    http_response_code(400);
    echo json_encode(['error' => 'El archivo excede el limite de 5 MB']);
    exit;
}

// 4. Nombre del fichero
// Por defecto se genera un nombre aleatorio (comportamiento original).
// Si la aplicacion envia el campo opcional "filename", se sanea y se utiliza como
// nombre definitivo: asi el codigo QR incrustado en el PDF (que contiene la URL final)
// coincide con la URL que se guarda en la base de datos.
// Es retrocompatible: los clientes que no envian "filename" siguen funcionando igual.
$uniqueName = bin2hex(random_bytes(16)) . '.pdf';
if (isset($_POST['filename']) && $_POST['filename'] !== '') {
    $candidate = basename(trim($_POST['filename']));  // basename evita path traversal
    if (preg_match('/^[A-Za-z0-9._-]+\.pdf$/', $candidate) && strlen($candidate) <= 120) {
        $uniqueName = $candidate;  // Si ya existe se sobrescribe (reintentos idempotentes)
    }
}
$targetPath = $uploadDir . $uniqueName;

// 5. Almacenar el archivo
if (!move_uploaded_file($file['tmp_name'], $targetPath)) {
    http_response_code(500);
    echo json_encode(['error' => 'Error al guardar el archivo']);
    exit;
}

// 6. Construir la URL de descarga directa HTTPS y devolverla
$baseUrl = 'https://denox.eu/DECA/deca_storage/';
$downloadUrl = $baseUrl . $uniqueName;
echo json_encode(['url' => $downloadUrl]);