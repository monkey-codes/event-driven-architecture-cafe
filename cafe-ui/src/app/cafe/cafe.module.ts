import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {CafeRoutingModule} from './cafe-routing.module';
import {CafeComponent} from './cafe.component';
import {MatCardModule} from '@angular/material/card';
import {MatButtonModule} from '@angular/material/button';
import {ServiceActivityCardComponent} from './service-activity-card/service-activity-card.component';
import {SpeechBubbleComponent} from './speech-bubble/speech-bubble.component';


@NgModule({
    declarations: [CafeComponent, ServiceActivityCardComponent,
        SpeechBubbleComponent],
    imports: [
        CommonModule,
        CafeRoutingModule,
        MatCardModule,
        MatButtonModule
    ]
})
export class CafeModule {
}
