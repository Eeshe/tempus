import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';
import { Theme, UserSettings } from '../../user/models/user.model';

interface PatchUserSettingsRequest {
  theme: Theme;
  currency: string;
}

@Service()
export class UserSettingsService {
  private readonly http: HttpClient = inject(HttpClient);
  private readonly url = '/api/v1/settings';

  patchTheme(theme: Theme): Observable<UserSettings> {
    return this.patch({ theme });
  }

  patchCurrency(currency: string): Observable<UserSettings> {
    return this.patch({ currency });
  }

  private patch(patchUserSettingsRequest: Partial<PatchUserSettingsRequest>): Observable<UserSettings> {
    return this.http.patch<UserSettings>(this.url, patchUserSettingsRequest, {
      withCredentials: true,
    });
  }
}
