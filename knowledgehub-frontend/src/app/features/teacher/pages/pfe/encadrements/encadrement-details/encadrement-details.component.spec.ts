import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EncadrementDetailsComponent } from './encadrement-details.component';

describe('EncadrementDetailsComponent', () => {
  let component: EncadrementDetailsComponent;
  let fixture: ComponentFixture<EncadrementDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [EncadrementDetailsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EncadrementDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
