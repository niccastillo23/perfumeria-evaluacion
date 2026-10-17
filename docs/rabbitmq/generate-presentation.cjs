// Generate the editable, branded EP4 presentation.
// Requires PptxGenJS: npm install --no-save pptxgenjs
const fs = require('fs');
const path = require('path');
const PptxGenJS = require('pptxgenjs');

const pptx = new PptxGenJS();
pptx.layout = 'LAYOUT_WIDE';
pptx.author = 'PerfumerIA';
pptx.subject = 'Evaluación Parcial N°4 — RabbitMQ';
pptx.title = 'PerfumerIA — Mensajería asíncrona con RabbitMQ';
pptx.company = 'DSY1107 Desarrollo Cloud Native I';
pptx.lang = 'es-CL';
pptx.theme = {
  headFontFace: 'Aptos Display',
  bodyFontFace: 'Aptos',
  lang: 'es-CL',
};
pptx.defineSlideMaster({
  title: 'MASTER',
  background: { color: 'F8F5EF' },
  objects: [],
  slideNumber: { x: 12.24, y: 7.14, w: 0.4, h: 0.18, color: '8C8376', fontFace: 'Aptos', fontSize: 8 },
});

const W = 13.333;
const H = 7.5;
const C = {
  ink: '191815',
  dark: '201E1A',
  dark2: '2A2722',
  cream: 'F8F5EF',
  white: 'FFFFFF',
  gold: 'B6935B',
  goldLight: 'E9DFC9',
  line: 'E4DED2',
  muted: '746E64',
  soft: 'F0ECE4',
  green: '55745A',
  greenLight: 'E5EEE4',
  red: '9D554A',
  redLight: 'F4E8E5',
  blue: '4C687D',
  blueLight: 'E8EEF2',
};

const root = path.resolve(__dirname, '../..');
const evidence = path.join(root, 'docs/evidence/rabbitmq');
const assets = path.join(__dirname, 'assets');
const out = path.join(__dirname, 'PRESENTACION.pptx');
const image = (name) => path.join(evidence, name);
const asset = (name) => path.join(assets, name);

const times = [
  '00:15', '00:55', '01:35', '02:05', '02:35', '03:15',
  '03:55', '04:25', '05:00', '05:40', '06:20', '06:45',
];

function rect(slide, x, y, w, h, fill, options = {}) {
  slide.addShape(options.round ? pptx.ShapeType.roundRect : pptx.ShapeType.rect, {
    x, y, w, h,
    rectRadius: options.radius || 0.08,
    fill: { color: fill, transparency: options.transparency || 0 },
    line: options.line ? { color: options.line, width: options.lineWidth || 0.8 } : { color: fill, transparency: 100 },
  });
}

function line(slide, x1, y1, x2, y2, color = C.line, width = 1.1, endArrowType) {
  slide.addShape(pptx.ShapeType.line, {
    x: x1, y: y1, w: x2 - x1, h: y2 - y1,
    line: { color, width, ...(endArrowType ? { endArrowType } : {}) },
  });
}

function text(slide, value, x, y, w, h, options = {}) {
  slide.addText(value, {
    x, y, w, h,
    fontFace: options.fontFace || 'Aptos',
    fontSize: options.fontSize || 14,
    color: options.color || C.ink,
    bold: options.bold || false,
    italic: options.italic || false,
    charSpacing: options.charSpacing || 0,
    margin: options.margin === undefined ? 0 : options.margin,
    breakLine: false,
    fit: 'shrink',
    valign: options.valign || 'mid',
    align: options.align || 'left',
    paraSpaceAfterPt: options.paraSpaceAfterPt || 0,
    isTextBox: true,
    ...(options.transparency ? { transparency: options.transparency } : {}),
  });
}

function pngDimensions(file) {
  const fd = fs.openSync(file, 'r');
  const header = Buffer.alloc(24);
  fs.readSync(fd, header, 0, 24, 0);
  fs.closeSync(fd);
  if (header.toString('ascii', 1, 4) !== 'PNG') throw new Error(`Expected PNG: ${file}`);
  return { w: header.readUInt32BE(16), h: header.readUInt32BE(20) };
}

function addImageContain(slide, file, x, y, w, h, inset = 0) {
  const { w: iw, h: ih } = pngDimensions(file);
  const maxW = w - inset * 2;
  const maxH = h - inset * 2;
  const scale = Math.min(maxW / iw, maxH / ih);
  const dw = iw * scale;
  const dh = ih * scale;
  slide.addImage({ path: file, x: x + (w - dw) / 2, y: y + (h - dh) / 2, w: dw, h: dh });
  return { x: x + (w - dw) / 2, y: y + (h - dh) / 2, w: dw, h: dh };
}

function frameImage(slide, file, x, y, w, h, options = {}) {
  rect(slide, x, y, w, h, options.fill || C.white, {
    round: true,
    line: options.line || C.line,
    lineWidth: 0.85,
  });
  return addImageContain(slide, file, x + 0.07, y + 0.07, w - 0.14, h - 0.14, 0);
}

function pill(slide, label, x, y, w, options = {}) {
  const h = options.h || 0.32;
  rect(slide, x, y, w, h, options.fill || C.goldLight, { round: true });
  text(slide, label, x + 0.11, y + 0.01, w - 0.22, h - 0.02, {
    fontSize: options.fontSize || 9,
    bold: true,
    color: options.color || C.ink,
    charSpacing: 0.5,
    align: options.align || 'center',
  });
}

function addBase(slide, index, section, dark = false) {
  const color = dark ? 'DAD2C4' : C.muted;
  const rule = dark ? '514A3E' : C.line;
  text(slide, 'PERFUMERIA  /  CLOUD NATIVE', 0.68, 0.26, 4.5, 0.2, {
    fontSize: 8,
    bold: true,
    color,
    charSpacing: 1.4,
  });
  text(slide, section.toUpperCase(), 8.0, 0.26, 4.65, 0.2, {
    fontSize: 8,
    bold: true,
    color,
    charSpacing: 1.2,
    align: 'right',
  });
  line(slide, 0.68, 7.02, 12.65, 7.02, rule, 0.7);
  text(slide, 'DSY1107  ·  EVALUACIÓN PARCIAL N°4', 0.68, 7.12, 5.4, 0.18, {
    fontSize: 7.5,
    color,
    charSpacing: 0.7,
  });
  text(slide, `${times[index - 1]}  ·  ${String(index).padStart(2, '0')} / 12`, 10.5, 7.11, 2.15, 0.18, {
    fontSize: 7.5,
    color,
    align: 'right',
    charSpacing: 0.7,
  });
}

function header(slide, eyebrow, titleText, subtitle) {
  text(slide, eyebrow.toUpperCase(), 0.72, 0.68, 9.5, 0.22, {
    fontSize: 9,
    bold: true,
    color: C.gold,
    charSpacing: 1.8,
  });
  text(slide, titleText, 0.72, 0.98, 11.95, 0.58, {
    fontFace: 'Aptos Display',
    fontSize: 27,
    bold: true,
    color: C.ink,
  });
  if (subtitle) text(slide, subtitle, 0.74, 1.62, 11.8, 0.34, { fontSize: 12, color: C.muted });
}

function addCard(slide, x, y, w, h, titleText, bodyText, options = {}) {
  rect(slide, x, y, w, h, options.fill || C.white, { round: true, line: options.line || C.line });
  if (options.number) {
    text(slide, options.number, x + 0.2, y + 0.18, 0.42, 0.35, { fontSize: 14, bold: true, color: C.gold });
  }
  const left = options.number ? 0.72 : 0.22;
  text(slide, titleText, x + left, y + 0.18, w - left - 0.2, options.body ? 0.34 : h - 0.34, {
    fontSize: options.titleSize || 13,
    bold: true,
    color: options.titleColor || C.ink,
    valign: options.body ? 'mid' : 'top',
  });
  if (bodyText) text(slide, bodyText, x + left, y + 0.58, w - left - 0.22, h - 0.74, {
    fontSize: options.bodySize || 10.5,
    color: options.bodyColor || C.muted,
    valign: 'top',
  });
}

function addNode(slide, x, y, w, h, titleText, bodyText, options = {}) {
  rect(slide, x, y, w, h, options.fill || C.white, { round: true, line: options.line || C.line });
  if (options.kicker) text(slide, options.kicker.toUpperCase(), x + 0.17, y + 0.13, w - 0.34, 0.18, {
    fontSize: 7.6, bold: true, color: options.accent || C.gold, charSpacing: 1,
  });
  text(slide, titleText, x + 0.17, y + (options.kicker ? 0.39 : 0.18), w - 0.34, 0.31, {
    fontSize: options.titleSize || 13, bold: true, color: options.titleColor || C.ink,
  });
  if (bodyText) text(slide, bodyText, x + 0.17, y + (options.kicker ? 0.78 : 0.56), w - 0.34, h - (options.kicker ? 0.9 : 0.68), {
    fontSize: options.bodySize || 9.5, color: options.bodyColor || C.muted, valign: 'top',
  });
}

// 01 — Cover
{
  const slide = pptx.addSlide();
  slide.background = { color: C.dark };
  rect(slide, 0, 0, 0.1, H, C.gold);
  slide.addImage({ path: path.join(root, 'frontend/web-app/public/logo.png'), x: 0.72, y: 0.52, w: 0.78, h: 0.78 });
  text(slide, 'PERFUMERIA', 1.62, 0.62, 2.3, 0.28, { fontFace: 'Aptos Display', fontSize: 16, bold: true, color: C.white, charSpacing: 2.3 });
  text(slide, 'ARTE EN FRAGANCIAS', 1.65, 0.96, 2.5, 0.2, { fontSize: 8, color: 'C9C0B3', charSpacing: 2.1 });
  text(slide, 'EVALUACIÓN PARCIAL N°4  ·  DSY1107', 0.78, 2.0, 5.7, 0.25, { fontSize: 9, bold: true, color: C.gold, charSpacing: 1.5 });
  text(slide, 'Una compra.\nTres procesos.\nCero bloqueos.', 0.76, 2.42, 6.2, 2.15, {
    fontFace: 'Aptos Display', fontSize: 34, bold: true, color: C.cream, valign: 'top',
  });
  text(slide, 'Mensajería asíncrona con RabbitMQ', 0.8, 4.85, 5.7, 0.38, { fontSize: 17, color: 'DAD2C4' });
  text(slide, 'Arquitectura cloud · seguridad · resiliencia', 0.8, 5.35, 5.8, 0.28, { fontSize: 10.5, color: 'AFA79B', charSpacing: 0.4 });
  text(slide, 'FRONTEND  →  API GATEWAY  →  EVENTOS', 0.8, 6.35, 5.8, 0.2, { fontSize: 8, bold: true, color: C.gold, charSpacing: 1.1 });
  rect(slide, 7.08, 1.22, 5.55, 4.86, C.dark2, { round: true, line: '514A3E' });
  text(slide, 'LA EXPERIENCIA DE COMPRA', 7.4, 1.48, 4.9, 0.22, { fontSize: 8.5, bold: true, color: 'DAD2C4', charSpacing: 1.3 });
  addImageContain(slide, asset('catalogo-sin-header.png'), 7.36, 1.9, 4.98, 2.78, 0);
  line(slide, 7.68, 5.0, 12.1, 5.0, '6D604B', 1.2);
  const labels = [
    ['01', 'Aviso'], ['02', 'Documento'], ['03', 'Envío'],
  ];
  labels.forEach((item, i) => {
    const x = 7.52 + i * 1.7;
    rect(slide, x, 5.33, 1.47, 0.48, '302C26', { round: true, line: '60533F' });
    text(slide, item[0], x + 0.12, 5.43, 0.32, 0.18, { fontSize: 8, bold: true, color: C.gold });
    text(slide, item[1], x + 0.47, 5.39, 0.9, 0.22, { fontSize: 9, color: C.cream });
  });
  addBase(slide, 1, 'Apertura', true);
}

// 02 — Architecture and code organization
{
  const slide = pptx.addSlide('MASTER');
  addBase(slide, 2, 'Arquitectura');
  header(slide, '01 · Diseño', 'Una ruta clara: del navegador al trabajo asíncrono', 'La interfaz no espera a que terminen las tareas secundarias del pedido.');

  const y = 2.32, h = 1.47;
  const nodes = [
    { x: 0.72, w: 1.78, k: 'EXPERIENCIA', t: 'React + MSAL', b: 'Login OIDC\nCatálogo y checkout', accent: C.gold },
    { x: 2.94, w: 1.92, k: 'PERÍMETRO', t: 'API Gateway', b: 'Rutas + JWT\nScopes por recurso', accent: C.blue },
    { x: 5.30, w: 1.55, k: 'FACHADA', t: 'BFF', b: 'Proxy seguro\nPuerto 8080', accent: C.blue },
    { x: 7.28, w: 2.50, k: 'DOMINIO', t: 'Microservicios', b: 'Catálogo · pedidos\nenvíos · Rabbit Admin', accent: C.green },
    { x: 10.20, w: 2.40, k: 'MENSAJERÍA', t: 'RabbitMQ × 2', b: 'Topic + direct\nACK · DLQ', accent: C.gold },
  ];
  nodes.forEach((n) => addNode(slide, n.x, y, n.w, h, n.t, n.b, { kicker: n.k, accent: n.accent, titleSize: 12.5, bodySize: 9.2 }));
  [[2.52, 2.94], [4.88, 5.3], [6.87, 7.28], [9.8, 10.2]].forEach(([x1, x2]) => line(slide, x1 + 0.05, 3.04, x2 - 0.06, 3.04, C.gold, 1.4, 'triangle'));

  text(slide, 'DISEÑO INTERNO', 0.76, 4.24, 2.5, 0.2, { fontSize: 8.5, bold: true, color: C.gold, charSpacing: 1.4 });
  const cy = 4.68;
  addCard(slide, 0.72, cy, 3.85, 1.38, 'application.yml', 'Centraliza nombres de colas, exchanges, routing keys y reintentos.', { number: '01', fill: C.white });
  addCard(slide, 4.74, cy, 3.85, 1.38, '@Configuration', 'Declara explícitamente Queue, Exchange y Binding por caso de uso.', { number: '02', fill: C.white });
  addCard(slide, 8.76, cy, 3.84, 1.38, 'Dominios desacoplados', 'Servicios publican; listeners por dominio procesan los eventos.', { number: '03', fill: C.white });
  pill(slide, 'CONFIGURACIÓN', 0.83, 6.31, 1.75, { h: 0.3, fontSize: 8 });
  text(slide, '→', 2.68, 6.31, 0.35, 0.3, { fontSize: 17, color: C.gold, align: 'center' });
  pill(slide, 'BEANS RABBITMQ', 3.1, 6.31, 1.8, { h: 0.3, fontSize: 8 });
  text(slide, '→', 5.03, 6.31, 0.35, 0.3, { fontSize: 17, color: C.gold, align: 'center' });
  pill(slide, 'LÓGICA DE DOMINIO', 5.48, 6.31, 2.0, { h: 0.3, fontSize: 8 });
}

// 03 — API Gateway, JWT and IDaaS
{
  const slide = pptx.addSlide('MASTER');
  addBase(slide, 3, 'Identidad y acceso');
  header(slide, '02 · Seguridad', 'La identidad se valida antes de entrar al backend', 'API Gateway enruta al BFF; Entra ID emite los tokens y los scopes delimitan el acceso.');
  frameImage(slide, image('13-api-gateway-jwt.png'), 0.72, 2.22, 6.15, 3.76);
  text(slide, 'EVIDENCIA COMPLEMENTARIA  ·  RUTA PROTEGIDA SIN TOKEN → 401', 0.86, 6.05, 5.9, 0.22, {
    fontSize: 8, bold: true, color: C.muted, charSpacing: 0.55,
  });

  pill(slide, 'AWS HTTP API  ·  us-east-1', 7.25, 2.22, 2.3, { h: 0.34, fontSize: 8.5 });
  text(slide, 'zu1dfduu53', 7.27, 2.73, 4.8, 0.43, { fontFace: 'Aptos Display', fontSize: 21, bold: true });
  const steps = [
    ['01', 'Microsoft Entra ID', 'Tenant y usuarios registrados'],
    ['02', 'JWT authorizer', 'Emisor, audiencia y firma verificados'],
    ['03', 'Ruta protegida', 'Admin.Read → BFF → Rabbit Admin'],
  ];
  steps.forEach((s, i) => {
    const yy = 3.38 + i * 0.73;
    rect(slide, 7.28, yy, 0.42, 0.42, i === 2 ? C.greenLight : C.goldLight, { round: true });
    text(slide, s[0], 7.31, yy + 0.04, 0.36, 0.3, { fontSize: 9, bold: true, color: i === 2 ? C.green : C.gold, align: 'center' });
    text(slide, s[1], 7.88, yy - 0.01, 4.55, 0.26, { fontSize: 12, bold: true });
    text(slide, s[2], 7.88, yy + 0.29, 4.55, 0.22, { fontSize: 9.3, color: C.muted });
  });
  rect(slide, 7.26, 5.8, 5.15, 0.72, C.dark, { round: true });
  text(slide, 'MOSTRAR EN VIVO', 7.48, 5.94, 1.5, 0.18, { fontSize: 8, bold: true, color: C.gold, charSpacing: 1 });
  text(slide, 'API Gateway: integración, ruta y authorizer azure-ad', 7.48, 6.17, 4.65, 0.2, { fontSize: 9.3, color: C.cream });
}

// 04 — OIDC frontend
{
  const slide = pptx.addSlide('MASTER');
  addBase(slide, 4, 'Experiencia autenticada');
  header(slide, '03 · Frontend', 'Una sesión OIDC habilita el flujo de compra', 'El frontend obtiene el token y consume los endpoints por el API Manager.');
  frameImage(slide, asset('catalogo-sin-header.png'), 0.72, 2.18, 7.55, 4.35);
  text(slide, 'CATÁLOGO WEB EN HTTPS', 0.9, 6.59, 4.0, 0.2, { fontSize: 8, bold: true, color: C.muted, charSpacing: 1 });

  text(slide, 'DEL LOGIN A LA API', 8.7, 2.34, 3.9, 0.22, { fontSize: 8.5, bold: true, color: C.gold, charSpacing: 1.4 });
  const authRows = [
    ['01', 'Autenticación', 'Microsoft Entra ID'],
    ['02', 'Protocolo', 'OAuth 2.0 + OpenID Connect'],
    ['03', 'Cliente', 'React + MSAL'],
    ['04', 'Scopes', 'Catalog.Read · Orders.Create'],
  ];
  authRows.forEach((r, i) => {
    const yy = 2.8 + i * 0.75;
    text(slide, r[0], 8.72, yy, 0.35, 0.24, { fontSize: 9, bold: true, color: C.gold });
    text(slide, r[1], 9.15, yy - 0.01, 2.85, 0.25, { fontSize: 11.2, bold: true });
    text(slide, r[2], 9.15, yy + 0.28, 3.1, 0.25, { fontSize: 9.3, color: C.muted });
    if (i < authRows.length - 1) line(slide, 9.15, yy + 0.62, 12.4, yy + 0.62, C.line, 0.7);
  });
  rect(slide, 8.7, 5.98, 3.84, 0.52, C.greenLight, { round: true });
  text(slide, 'LOGIN REAL  ·  CATÁLOGO CARGADO', 8.9, 6.12, 3.45, 0.2, { fontSize: 8.4, bold: true, color: C.green, charSpacing: 0.5 });
}

// 05 — Cloud deployment
{
  const slide = pptx.addSlide('MASTER');
  addBase(slide, 5, 'Despliegue cloud');
  header(slide, '04 · Operación', 'La solución está desplegada e integrada en AWS', 'Frontend HTTPS y servicios backend activos en la instancia EC2.');
  frameImage(slide, image('14-cloud-deployment.png'), 0.72, 2.18, 7.45, 4.55);
  text(slide, 'EVIDENCIA DE SERVICIOS ACTIVOS Y RESPUESTA HTTP 200', 0.89, 6.78, 7.1, 0.15, { fontSize: 7.7, bold: true, color: C.muted, charSpacing: 0.5 });

  pill(slide, 'PRODUCCIÓN · HTTPS', 8.55, 2.22, 2.03, { h: 0.32, fill: C.greenLight, color: C.green, fontSize: 8 });
  const services = [
    ['WEB', 'React + nginx'],
    ['API', 'BFF · catálogo · pedidos'],
    ['EVENTOS', 'shipping · Rabbit Admin'],
  ];
  services.forEach((s, i) => {
    const yy = 2.86 + i * 0.88;
    rect(slide, 8.56, yy, 3.94, 0.67, C.white, { round: true, line: C.line });
    text(slide, s[0], 8.76, yy + 0.1, 0.9, 0.18, { fontSize: 8, bold: true, color: C.gold, charSpacing: 0.9 });
    text(slide, s[1], 8.76, yy + 0.34, 3.45, 0.2, { fontSize: 10.2, bold: true });
  });
  rect(slide, 8.55, 5.72, 3.94, 0.76, C.dark, { round: true });
  text(slide, 'RabbitMQ no se expone a Internet', 8.77, 5.87, 3.5, 0.22, { fontSize: 10, bold: true, color: C.cream });
  text(slide, 'AMQP y Management limitados a loopback', 8.77, 6.19, 3.5, 0.18, { fontSize: 8.5, color: 'DAD2C4' });
}

// 06 — Event flow
{
  const slide = pptx.addSlide('MASTER');
  addBase(slide, 6, 'Flujo asíncrono');
  header(slide, '05 · Del pedido al evento', 'El checkout publica; cada dominio resuelve su tarea', 'El trabajo secundario se procesa en paralelo y no se mezcla con la respuesta al cliente.');

  frameImage(slide, asset('checkout-sin-header.png'), 0.72, 2.22, 5.08, 3.96);
  text(slide, 'CHECKOUT  ·  CONFIRMAR Y PAGAR', 0.9, 6.27, 4.65, 0.2, { fontSize: 8, bold: true, color: C.muted, charSpacing: 0.8 });

  addNode(slide, 6.25, 2.24, 2.0, 1.05, 'Order service', 'Confirma pedido\ny publica eventos', { kicker: 'PRODUCTOR', accent: C.gold, titleSize: 12, bodySize: 9 });
  line(slide, 8.28, 2.76, 8.73, 2.76, C.gold, 1.4, 'triangle');
  addNode(slide, 8.76, 2.24, 1.58, 1.05, 'RabbitMQ', 'Exchange + routing', { kicker: 'BROKER', accent: C.blue, titleSize: 11, bodySize: 8.5 });
  line(slide, 10.37, 2.76, 10.83, 2.76, C.gold, 1.4, 'triangle');
  addNode(slide, 10.87, 2.24, 1.68, 1.05, 'Listeners', 'Procesan por dominio', { kicker: 'CONSUMO', accent: C.green, titleSize: 11, bodySize: 8.5 });

  const eventRows = [
    ['orders.created / orders.paid', 'Notificación + documento', C.blueLight, C.blue],
    ['shipping.requested', 'Solicitud de envío', C.greenLight, C.green],
  ];
  eventRows.forEach((r, i) => {
    const yy = 3.72 + i * 0.82;
    rect(slide, 6.25, yy, 6.3, 0.65, r[2], { round: true });
    text(slide, r[0], 6.47, yy + 0.1, 2.8, 0.2, { fontSize: 9.1, bold: true, color: r[3] });
    text(slide, '→', 9.37, yy + 0.09, 0.36, 0.24, { fontSize: 15, color: C.gold, align: 'center' });
    text(slide, r[1], 9.9, yy + 0.1, 2.45, 0.2, { fontSize: 9.2, bold: true, color: C.ink });
  });
  text(slide, 'OrderEventPublisher  ·  OrderNotificationListener  ·  OrderDocumentListener  ·  ShipmentRequestedListener', 6.27, 5.77, 6.25, 0.42, {
    fontSize: 8.3, color: C.muted, valign: 'top',
  });
  pill(slide, 'CONSUMIDORES AGRUPADOS POR DOMINIO', 6.25, 6.34, 3.47, { h: 0.3, fontSize: 7.8 });
}

// 07 — Exchanges and routing
{
  const slide = pptx.addSlide('MASTER');
  addBase(slide, 7, 'Topología');
  header(slide, '06 · Ruteo', 'Dos estrategias de routing para dos tipos de evento', 'Bindings explícitos: el exchange decide qué consumidor recibe cada mensaje.');
  frameImage(slide, image('05-exchanges-bindings.png'), 6.25, 2.16, 6.35, 4.55);

  rect(slide, 0.72, 2.2, 5.05, 1.64, C.white, { round: true, line: C.line });
  pill(slide, 'TOPIC', 0.96, 2.43, 0.94, { h: 0.32, fill: C.blueLight, color: C.blue });
  text(slide, 'perfumeria.orders.topic', 2.06, 2.43, 3.35, 0.3, { fontSize: 11.3, bold: true });
  text(slide, 'orders.* → notificaciones   ·   orders.paid → documentos', 0.97, 2.99, 4.53, 0.45, { fontSize: 10.3, color: C.muted, valign: 'top' });

  rect(slide, 0.72, 4.02, 5.05, 1.38, C.white, { round: true, line: C.line });
  pill(slide, 'DIRECT', 0.96, 4.25, 0.94, { h: 0.32, fill: C.greenLight, color: C.green });
  text(slide, 'perfumeria.shipping.direct', 2.06, 4.25, 3.35, 0.3, { fontSize: 11.1, bold: true });
  text(slide, 'shipping.requested → solicitudes de envío', 0.97, 4.79, 4.53, 0.28, { fontSize: 10.3, color: C.muted });

  rect(slide, 0.72, 5.65, 5.05, 0.8, C.dark, { round: true });
  text(slide, 'DLX', 0.98, 5.82, 0.5, 0.22, { fontSize: 9, bold: true, color: C.gold });
  text(slide, 'perfumeria.dead-letter.direct', 1.6, 5.8, 3.88, 0.25, { fontSize: 10, bold: true, color: C.cream });
  text(slide, 'Mensajes rechazados → cola de errores del dominio', 0.98, 6.15, 4.47, 0.19, { fontSize: 8.4, color: 'DAD2C4' });
}

// 08 — Queues and DLQs
{
  const slide = pptx.addSlide('MASTER');
  addBase(slide, 8, 'Colas durables y DLQ');
  header(slide, '07 · Persistencia', 'Cada caso de uso tiene su cola y su DLQ', 'Seis colas visibles en RabbitMQ Management: tres de trabajo y tres de fallos.');
  frameImage(slide, image('06-queues-dlq.png'), 0.72, 2.17, 7.22, 4.5);

  text(slide, '3 FLUJOS  /  3 DLQ', 8.35, 2.28, 3.8, 0.28, { fontSize: 9, bold: true, color: C.gold, charSpacing: 1.4 });
  const pairs = [
    ['orders.notifications', 'orders.notifications.dlq', C.blue],
    ['orders.documents', 'orders.documents.dlq', C.gold],
    ['shipping.requests', 'shipping.requests.dlq', C.green],
  ];
  pairs.forEach((p, i) => {
    const yy = 2.78 + i * 1.02;
    rect(slide, 8.34, yy, 4.24, 0.79, C.white, { round: true, line: C.line });
    rect(slide, 8.49, yy + 0.18, 0.08, 0.43, p[2], { round: true });
    text(slide, p[0], 8.73, yy + 0.12, 3.57, 0.23, { fontSize: 9.3, bold: true });
    text(slide, '↓  DLQ', 8.73, yy + 0.43, 0.76, 0.2, { fontSize: 8.2, bold: true, color: p[2] });
    text(slide, p[1], 9.53, yy + 0.43, 2.75, 0.2, { fontSize: 8.1, color: C.muted });
  });
  rect(slide, 8.34, 6.0, 4.24, 0.57, C.goldLight, { round: true });
  text(slide, 'DURABLES  ·  DEAD-LETTER EXCHANGE', 8.55, 6.17, 3.83, 0.18, { fontSize: 8.2, bold: true, color: C.ink, charSpacing: 0.4 });
}

// 09 — Compose and cluster
{
  const slide = pptx.addSlide('MASTER');
  addBase(slide, 9, 'Clúster en la nube');
  header(slide, '08 · Infraestructura', 'Dos nodos unidos; un solo clúster operativo', 'Los contenedores se levantan con Docker Compose en la EC2 de AWS.');
  frameImage(slide, image('01-compose-containers.png'), 0.72, 2.18, 7.15, 4.35);
  text(slide, 'docker compose ps  ·  ambos nodos healthy', 0.91, 6.58, 6.8, 0.2, { fontSize: 8, bold: true, color: C.muted, charSpacing: 0.5 });
  frameImage(slide, asset('cluster-status-resumen.png'), 8.18, 2.18, 4.42, 2.97);
  rect(slide, 8.18, 5.35, 4.42, 1.18, C.dark, { round: true });
  text(slide, 'rabbit@rabbitmq1', 8.42, 5.54, 3.7, 0.2, { fontSize: 10.5, bold: true, color: C.cream });
  text(slide, 'rabbit@rabbitmq2', 8.42, 5.86, 3.7, 0.2, { fontSize: 10.5, bold: true, color: C.cream });
  pill(slide, 'RUNNING  ·  NO ALARMS', 8.4, 6.18, 2.42, { h: 0.26, fill: '354237', color: 'D6E5D5', fontSize: 7.3 });
}

// 10 — ACK, retry and DLQ
{
  const slide = pptx.addSlide('MASTER');
  addBase(slide, 10, 'Confirmación y errores');
  header(slide, '09 · Resiliencia', 'El ACK llega después del procesamiento', 'Los errores recuperables se reintentan; los mensajes inválidos terminan aislados en DLQ.');
  frameImage(slide, image('10-async-consumers.png'), 0.72, 2.18, 6.7, 4.45);

  text(slide, 'DECISIÓN POR RESULTADO', 7.82, 2.22, 4.45, 0.22, { fontSize: 8.5, bold: true, color: C.gold, charSpacing: 1.3 });
  const decisions = [
    ['PROCESADO', 'ACK manual', C.greenLight, C.green],
    ['ERROR TRANSITORIO', 'Hasta 3 intentos', C.blueLight, C.blue],
    ['INVÁLIDO / AGOTADO', 'NACK sin requeue → DLQ', C.redLight, C.red],
  ];
  decisions.forEach((d, i) => {
    const yy = 2.68 + i * 0.78;
    rect(slide, 7.82, yy, 4.75, 0.61, d[2], { round: true });
    text(slide, d[0], 8.02, yy + 0.08, 2.0, 0.18, { fontSize: 7.5, bold: true, color: d[3], charSpacing: 0.7 });
    text(slide, d[1], 8.02, yy + 0.31, 4.28, 0.2, { fontSize: 10, bold: true, color: C.ink });
  });
  frameImage(slide, image('11-dlq-error.png'), 7.82, 5.18, 4.75, 1.38);
  text(slide, 'MENSAJE DE PRUEBA CONTROLADO EN LA DLQ', 7.94, 6.61, 4.45, 0.18, { fontSize: 7.2, bold: true, color: C.muted, charSpacing: 0.35 });
}

// 11 — Admin API and rubric implementation
{
  const slide = pptx.addSlide('MASTER');
  addBase(slide, 11, 'Administración y diseño');
  header(slide, '10 · Administración', 'Gestión RabbitMQ con responsabilidades separadas', 'API REST validada; el controlador delega en RabbitAdminService.');
  frameImage(slide, image('12-rabbit-admin-api.png'), 0.72, 2.16, 6.25, 4.56);

  const adminRows = [
    ['API REST', 'POST /queues · /exchanges · /bindings'],
    ['SERVICIO', 'RabbitAdminService encapsula AmqpAdmin'],
    ['VALIDACIÓN', 'DTO + Bean Validation → 400 claro'],
    ['SEGURIDAD', 'Admin.Read exigido en la ruta protegida'],
  ];
  adminRows.forEach((r, i) => {
    const yy = 2.2 + i * 0.78;
    text(slide, r[0], 7.42, yy, 1.25, 0.2, { fontSize: 8.1, bold: true, color: C.gold, charSpacing: 0.75 });
    text(slide, r[1], 7.42, yy + 0.26, 5.02, 0.31, { fontSize: 10.4, bold: true });
    if (i < adminRows.length - 1) line(slide, 7.42, yy + 0.67, 12.48, yy + 0.67, C.line, 0.7);
  });
  rect(slide, 7.42, 5.62, 5.02, 0.92, C.dark, { round: true });
  text(slide, '201 creado   ·   204 eliminado   ·   400 inválido', 7.67, 5.8, 4.57, 0.23, { fontSize: 10, bold: true, color: C.cream, align: 'center' });
  text(slide, 'Controlador fino  /  servicio dedicado  /  entrada validada', 7.67, 6.16, 4.57, 0.18, { fontSize: 8.3, color: 'DAD2C4', align: 'center' });
}

// 12 — Conclusion
{
  const slide = pptx.addSlide();
  slide.background = { color: C.dark };
  rect(slide, 0, 0, 0.1, H, C.gold);
  addBase(slide, 12, 'Cierre', true);
  text(slide, 'RESULTADO', 0.78, 0.83, 3.2, 0.22, { fontSize: 9, bold: true, color: C.gold, charSpacing: 1.7 });
  text(slide, 'El pedido avanza.\nLa experiencia no espera.', 0.76, 1.32, 8.9, 1.52, {
    fontFace: 'Aptos Display', fontSize: 31, bold: true, color: C.cream, valign: 'top',
  });
  text(slide, 'Prueba extremo a extremo en la nube', 0.8, 3.12, 5.5, 0.34, { fontSize: 15, color: 'DAD2C4' });

  const resultCards = [
    ['PEDIDO', 'PAID', 'US$125.50', C.gold],
    ['EVENTOS', '3 publicados', 'aviso · documento · envío', '8AA0B0'],
    ['FALLO', 'Aislado', 'mensaje inválido → DLQ', 'B98A7E'],
  ];
  resultCards.forEach((r, i) => {
    const x = 0.8 + i * 4.08;
    rect(slide, x, 4.22, 3.76, 1.36, '2B2823', { round: true, line: '514A3E' });
    text(slide, r[0], x + 0.22, 4.42, 3.25, 0.17, { fontSize: 8, bold: true, color: r[3], charSpacing: 1.1 });
    text(slide, r[1], x + 0.22, 4.72, 3.25, 0.33, { fontFace: 'Aptos Display', fontSize: 18, bold: true, color: C.cream });
    text(slide, r[2], x + 0.22, 5.16, 3.25, 0.2, { fontSize: 9, color: 'C9C0B3' });
  });
  text(slide, 'Desacoplamiento  ·  seguridad JWT  ·  reintentos  ·  DLQ', 0.82, 6.12, 8.5, 0.28, { fontSize: 10.5, bold: true, color: C.gold, charSpacing: 0.4 });
  text(slide, 'Preguntas', 10.5, 6.09, 1.95, 0.3, { fontFace: 'Aptos Display', fontSize: 16, bold: true, color: C.cream, align: 'right' });
}

async function main() {
  await pptx.writeFile({ fileName: out });
  process.stdout.write(`Presentación generada: ${out}\n`);
}

main().catch((error) => {
  process.stderr.write(`${error.stack || error}\n`);
  process.exitCode = 1;
});
