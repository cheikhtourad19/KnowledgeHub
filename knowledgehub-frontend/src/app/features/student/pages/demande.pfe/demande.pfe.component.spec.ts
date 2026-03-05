import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DemandePfeComponent } from './demande.pfe.component';

describe('DemandePfeComponent', () => {
  let component: DemandePfeComponent;
  let fixture: ComponentFixture<DemandePfeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DemandePfeComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DemandePfeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
