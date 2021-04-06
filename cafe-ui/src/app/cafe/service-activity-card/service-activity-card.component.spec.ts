import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ServiceActivityCardComponent } from './service-activity-card.component';

describe('ServiceActivityCardComponent', () => {
  let component: ServiceActivityCardComponent;
  let fixture: ComponentFixture<ServiceActivityCardComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ServiceActivityCardComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ServiceActivityCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
