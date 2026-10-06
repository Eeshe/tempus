import { Component } from '@angular/core';
import { ClientList } from '../client-list/client-list';

@Component({
  imports: [ClientList],
  selector: 'app-clients-page',
  styleUrl: './clients-page.css',
  templateUrl: './clients-page.html',
})
export class ClientsPage {
}
