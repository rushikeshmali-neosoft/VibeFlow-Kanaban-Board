import { UserModel } from './user.model';

export interface AuthResponse {
  token: string;
  email: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
}

export interface AuthState {
  token: string | null;
  currentUser: UserModel | null;
}
