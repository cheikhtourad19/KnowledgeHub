import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PfeTableComponent } from './pfe-table.component';

describe('PfeTableComponent', () => {
  let component: PfeTableComponent;
  let fixture: ComponentFixture<PfeTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PfeTableComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PfeTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
