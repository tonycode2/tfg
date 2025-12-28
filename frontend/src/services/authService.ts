const API_URL = 'http://localhost:8080';

export type Role = 'ADMIN' | 'HR' | 'JEFE' | 'EMPLEADO';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
}

export interface UserInfo {
  username: string;
  role: Role;
}

export const authService = {
  async login(credentials: LoginRequest): Promise<AuthResponse> {
    const response = await fetch(`${API_URL}/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(credentials),
    });

    if (!response.ok) {
      throw new Error('Error al iniciar sesión');
    }

    return response.json();
  },

  async register(userData: RegisterRequest): Promise<AuthResponse> {
    const response = await fetch(`${API_URL}/auth/register`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(userData),
    });

    if (!response.ok) {
      throw new Error('Error al registrarse');
    }

    return response.json();
  },

  saveToken(token: string): void {
    localStorage.setItem('token', token);
  },

  getToken(): string | null {
    return localStorage.getItem('token');
  },

  removeToken(): void {
    localStorage.removeItem('token');
  },

  isAuthenticated(): boolean {
    return !!this.getToken();
  },

  // Función temporal para simular usuario - será reemplazada cuando conectemos al backend
  getUserInfo(): UserInfo {
    // TODO: Decodificar el token JWT para obtener el rol real
    // Por ahora retornamos un usuario de prueba
    return {
      username: localStorage.getItem('username') || 'Usuario',
      role: (localStorage.getItem('role') as Role) || 'EMPLEADO',
    };
  },

  // Función temporal para establecer rol de prueba
  setUserInfo(username: string, role: Role): void {
    localStorage.setItem('username', username);
    localStorage.setItem('role', role);
  },

  logout(): void {
    this.removeToken();
    localStorage.removeItem('username');
    localStorage.removeItem('role');
  },
};
