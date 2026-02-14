/**
 * UserBase
 * Abstract base class for all user types
 */
export interface UserBase {
  authId: string;
  email: string;
  nom: string;
  prenom: string;
}
