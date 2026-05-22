export enum TypeAnnonceStage {
  TECHNICIEN = 'TECHNICIEN',
  OUVRIER    = 'OUVRIER',
  FIN_ETUDES = 'FIN_ETUDES',
  LIBRE      = 'LIBRE',
}

export const TypeAnnonceStageLabels: Record<TypeAnnonceStage, string> = {
  [TypeAnnonceStage.TECHNICIEN]: 'Stage technicien',
  [TypeAnnonceStage.OUVRIER]:    'Stage ouvrier',
  [TypeAnnonceStage.FIN_ETUDES]: "Stage fin d'études",
  [TypeAnnonceStage.LIBRE]:      'Stage libre',
};