import {AfterViewInit, Component, Input, OnInit, Output} from '@angular/core';
import { EventEmitter } from '@angular/core';

@Component({
    selector: 'app-speech-bubble',
    templateUrl: './speech-bubble.component.html',
    styleUrls: ['./speech-bubble.component.scss']
})
export class SpeechBubbleComponent implements OnInit, AfterViewInit {

    @Input() message: string;

    @Output() closeBubble = new EventEmitter();

    constructor() {
    }

    ngOnInit(): void {

    }

    ngAfterViewInit(): void {
        setTimeout( this.onCloseBubble.bind(this), 2000);
    }

    public onCloseBubble() {
        this.closeBubble.emit(null);
    }

}
