const express = require('express');
const admin = require('firebase-admin');

// Inicializar Firebase Admin SDK
// Descarga tu serviceAccountKey.json de Firebase Console > Configuración del proyecto > Cuentas de servicio
// Colócalo en la misma carpeta que este archivo
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const app = express();
const port = 3000;

// Middleware para parsear JSON
app.use(express.json());
app.use(express.static('public')); // Para servir archivos estáticos como CSS si los agregas

// Endpoint para enviar mensaje FCM
app.post('/send', async (req, res) => {
  const { token, title, body } = req.body;

  if (!token || !title || !body) {
    return res.status(400).json({ error: 'Faltan parámetros: token, title, body' });
  }

  const message = {
    token: token,
    notification: {
      title: title,
      body: body
    }
  };

  try {
    const response = await admin.messaging().send(message);
    console.log('Mensaje enviado exitosamente:', response);
    res.json({ success: true, messageId: response });
  } catch (error) {
    console.error('Error enviando mensaje:', error);
    res.status(500).json({ error: 'Error enviando mensaje', details: error.message });
  }
});

// Servir la página HTML
app.get('/', (req, res) => {
  res.sendFile(__dirname + '/index.html');
});

app.listen(port, () => {
  console.log(`Servidor corriendo en http://localhost:${port}`);
});
