import { TypeAnnonceStage } from '../enums/type-annonce-stage.enum';
import { ModaliteTravail } from '../enums/modalite-travail.enum';
import { TypeUrl } from '../enums/type-url.enum';

export interface AnnonceUrl {
  id?: number;
  url: string;
  typeUrl: TypeUrl;
}

export interface AnnonceStage {
  id?: number;
  titre: string;
  description: string;
  nomEntreprise: string;
  ville: string;
  pays: string;
  estActive: boolean;
  imageUrl?: string;
  typesStage: TypeAnnonceStage[];
  modaliteTravail: ModaliteTravail;
  urls: AnnonceUrl[];
}

export interface AnnonceStageRequest {
  titre: string;
  description: string;
  nomEntreprise: string;
  ville: string;
  pays: string;
  typesStage: TypeAnnonceStage[];
  modaliteTravail: ModaliteTravail;
  urls: AnnonceUrl[];
}
