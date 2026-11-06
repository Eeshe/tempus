import { inject, Service } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { RouterStateSnapshot, TitleStrategy } from '@angular/router';

const ROUTE_TITLE_TRANSLATIONS: Readonly<Record<string, () => string>> = {
  'route.register': () => $localize`:Browser tab title|Register page:Register`,
  'route.login': () => $localize`:Browser tab title|Login page:Login`,
  'route.home': () => $localize`:Browser tab title|Home page:Home`,
  'route.reports': () => $localize`:Browser tab title|Reports page:Reports`,
  'route.projects': () => $localize`:Browser tab title|Projects page:Projects`,
  'route.clients': () => $localize`:Browser tab title|Clients page:Clients`,
  'route.settings': () => $localize`:Browser tab title|Settings page:Settings`,
  'route.settings.migration': () => $localize`:Browser tab title|Migration settings page:Migration`,
  'route.settings.language': () => $localize`:Browser tab title|Language settings page:Language`,
  'route.settings.appearance': () => $localize`:Browser tab title|Appearance settings page:Appearance`,
};

@Service()
export class LocalizedTitleStrategy extends TitleStrategy {
  private readonly title: Title = inject(Title);

  override updateTitle(snapshot: RouterStateSnapshot): void {
    const routeTitle: string | undefined = this.buildTitle(snapshot);
    if (routeTitle == null) {
      return;
    }
    const translatedTitle: string = ROUTE_TITLE_TRANSLATIONS[routeTitle]?.() ?? routeTitle;
    this.title.setTitle(translatedTitle);
  }
}
