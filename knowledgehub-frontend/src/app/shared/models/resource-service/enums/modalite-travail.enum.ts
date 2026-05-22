export enum ModaliteTravail {
  PRESENTIEL = 'PRESENTIEL',
  EN_LIGNE   = 'EN_LIGNE',
  HYBRIDE    = 'HYBRIDE',
}

export const ModaliteTravailLabels: Record<ModaliteTravail, string> = {
  [ModaliteTravail.PRESENTIEL]: 'Présentiel',
  [ModaliteTravail.EN_LIGNE]:   'En ligne',
  [ModaliteTravail.HYBRIDE]:    'Hybride',
};

export const ModaliteTravailIcons: Record<ModaliteTravail, string> = {
  [ModaliteTravail.PRESENTIEL]: 'pi pi-building',
  [ModaliteTravail.EN_LIGNE]:   'pi pi-desktop',
  [ModaliteTravail.HYBRIDE]:    'pi pi-sync',
};