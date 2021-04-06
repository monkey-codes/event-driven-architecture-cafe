import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { CafeComponent } from './cafe.component';

describe('ContentComponent', () => {
  let component: CafeComponent;
  let fixture: ComponentFixture<CafeComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ CafeComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CafeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
