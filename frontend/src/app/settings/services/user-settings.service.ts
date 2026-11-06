import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';
import { Theme, UserSettings } from '../../user/models/user.model';

interface PatchUserSettingsRequest {
  theme: Theme;
}

@Service()
export class UserSettingsService {
  private readonly http: HttpClient = inject(HttpClient);
  private readonly url = "/api/v1/settings";

  patchTheme(theme: Theme): Observable<UserSettings> {
    const patchUserSettingsRequest: PatchUserSettingsRequest = { theme };

    return this.http.patch<UserSettings>(this.url, patchUserSettingsRequest, {
      withCredentials: true,
    });
  }
}
