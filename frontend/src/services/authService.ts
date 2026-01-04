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
      const payload = JSON.parse(atob(token.split('.')[1]));
      return {
        username: payload.sub || 'Usuario',
        role: payload.role || 'EMPLEADO',
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
