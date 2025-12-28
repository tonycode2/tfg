# Frontend - Sistema de Gestión de RH

Frontend desarrollado con React, TypeScript, Vite y shadcn/ui para el Sistema de Gestión de Recursos Humanos de la Sastrería Gerson Andre.

## 🚀 Tecnologías

- **React 18** con TypeScript
- **Vite** - Build tool y dev server
- **Tailwind CSS** - Estilos
- **shadcn/ui** - Componentes UI
- **React Router** - Navegación
- **Radix UI** - Primitivos de UI

## 📋 Requisitos Previos

- Node.js 18 o superior
- Backend Spring Boot corriendo en `http://localhost:8080`

## 🛠️ Instalación

```bash
cd frontend
npm install
```

## 🎮 Comandos Disponibles

### Desarrollo
```bash
npm run dev
```
Inicia el servidor de desarrollo en `http://localhost:5173`

### Build
```bash
npm run build
```
Genera la versión de producción en la carpeta `dist`

### Preview
```bash
npm run preview
```
Vista previa de la versión de producción

## 📁 Estructura del Proyecto

```
frontend/
├── src/
│   ├── components/
│   │   └── ui/          # Componentes de shadcn/ui
│   │       ├── button.tsx
│   │       ├── card.tsx
│   │       ├── input.tsx
│   │       └── label.tsx
│   ├── pages/           # Páginas de la aplicación
│   │   ├── LoginPage.tsx
│   │   ├── RegisterPage.tsx
│   │   └── DashboardPage.tsx
│   ├── services/        # Servicios API
│   │   └── authService.ts
│   ├── lib/
│   │   └── utils.ts     # Utilidades
│   ├── App.tsx          # Componente principal con router
│   ├── main.tsx         # Punto de entrada
│   └── index.css        # Estilos globales y variables Tailwind
├── public/
├── index.html
├── vite.config.ts
├── tailwind.config.js
├── tsconfig.json
└── package.json
```

## 🔐 Autenticación

El sistema implementa autenticación JWT con las siguientes rutas:

- `/login` - Página de inicio de sesión
- `/register` - Página de registro
- `/dashboard` - Dashboard protegido (requiere autenticación)
- `/` - Redirige a `/login`

### Flujo de Autenticación

1. El usuario se registra o inicia sesión
2. El backend devuelve un token JWT
3. El token se guarda en `localStorage`
4. Las rutas protegidas verifican la existencia del token
5. El token se envía en las peticiones autenticadas

## 🎨 Componentes UI

Los componentes de shadcn/ui están configurados y listos para usar:

- **Button** - Botones con variantes (default, outline, ghost, etc.)
- **Card** - Tarjetas con header, content y footer
- **Input** - Campos de entrada de texto
- **Label** - Etiquetas para formularios

## 🔗 Integración con Backend

La URL del backend está configurada en `src/services/authService.ts`:

```typescript
const API_URL = 'http://localhost:8080';
```

### Endpoints utilizados:
- `POST /auth/login` - Inicio de sesión
- `POST /auth/register` - Registro de usuario

## 🌐 CORS

El backend está configurado para aceptar peticiones desde `http://localhost:5173`.

## 📝 Notas de Desarrollo

- Los estilos utilizan variables CSS para temas claro/oscuro
- Todos los componentes UI son customizables vía Tailwind
- El proyecto usa path aliases (`@/`) para imports más limpios
- TypeScript en modo estricto para mayor seguridad de tipos
import reactDom from 'eslint-plugin-react-dom'

export default defineConfig([
  globalIgnores(['dist']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      // Other configs...
      // Enable lint rules for React
      reactX.configs['recommended-typescript'],
      // Enable lint rules for React DOM
      reactDom.configs.recommended,
    ],
    languageOptions: {
      parserOptions: {
        project: ['./tsconfig.node.json', './tsconfig.app.json'],
        tsconfigRootDir: import.meta.dirname,
      },
      // other options...
    },
  },
])
```
