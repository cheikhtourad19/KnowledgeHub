export enum TypeUrl {
  CANDIDATURE = 'CANDIDATURE',
  SITE_ENTREPRISE = 'SITE_ENTREPRISE',
  LINKEDIN = 'LINKEDIN',
  DOCUMENT = 'DOCUMENT',
  AUTRE = 'AUTRE',
}

export const TypeUrlLabels: Record<TypeUrl, string> = {
  [TypeUrl.CANDIDATURE]: 'Lien de candidature',
  [TypeUrl.SITE_ENTREPRISE]: "Site de l'entreprise",
  [TypeUrl.LINKEDIN]: 'Page LinkedIn',
  [TypeUrl.DOCUMENT]: 'Document joint',
  [TypeUrl.AUTRE]: 'Autre',
};

export const TypeUrlIcons: Record<TypeUrl, string> = {
  [TypeUrl.CANDIDATURE]: 'pi pi-send',
  [TypeUrl.SITE_ENTREPRISE]: 'pi pi-globe',
  [TypeUrl.LINKEDIN]: 'pi pi-linkedin',
  [TypeUrl.DOCUMENT]: 'pi pi-file',
  [TypeUrl.AUTRE]: 'pi pi-link',
};
