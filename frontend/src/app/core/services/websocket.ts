import { Injectable, NgZone } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Subject } from 'rxjs';
import { environment } from '../../../environments/environment';
import { WebSocketEventModel } from '../models/websocket-event.model';

@Injectable({
  providedIn: 'root',
})
export class WebsocketService {
  private client?: Client;
  private readonly eventSubject = new Subject<WebSocketEventModel>();

  readonly events$ = this.eventSubject.asObservable();

  constructor(private readonly ngZone: NgZone) {}

  connect(): void {
    if (this.client?.active) {
      return;
    }

    this.client = new Client({
      webSocketFactory: () => new SockJS(environment.websocketUrl),
      reconnectDelay: 5000,
      onConnect: () => {
        this.client?.subscribe('/topic/tasks', (message: IMessage) => {
          this.ngZone.run(() => {
            const event = JSON.parse(message.body) as WebSocketEventModel;
            this.eventSubject.next(event);
          });
        });
      },
    });

    this.client.activate();
  }

  disconnect(): void {
    this.client?.deactivate();
    this.client = undefined;
  }
}
