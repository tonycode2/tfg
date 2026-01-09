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
  passwordChangeRequired?: boolean;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface UserInfo {
  username: string;
  role: Role;
  nombreCompleto?: string;
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

  // Decodificar el token JWT para obtener la información del usuario
  getUserInfo(): UserInfo {
    const token = this.getToken();
    if (!token) {
      return {
        username: 'Usuario',
        role: 'EMPLEADO',
      };
    }

    try {
      // Decodificar el JWT (el payload es la parte central del token)
      // Usar decodeURIComponent + escape para manejar UTF-8 correctamente
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      const payload = JSON.parse(jsonPayload);
      return {
        username: payload.sub || 'Usuario',
        role: payload.role || 'EMPLEADO',
        nombreCompleto: payload.nombreCompleto,
      };
    } catch (error) {
      console.error('Error al decodificar el token:', error);
      return {
        username: 'Usuario',
        role: 'EMPLEADO',
      };
    }
  },

  logout(): void {
    this.removeToken();
  },

  async changePassword(request: ChangePasswordRequest): Promise<void> {
    const token = this.getToken();
    const response = await fetch(`${API_URL}/auth/change-password`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
      },
      body: JSON.stringify(request),
    });

    if (!response.ok) {
      const error = await response.text();
      throw new Error(error || 'Error al cambiar la contraseña');
    }
  },
};
