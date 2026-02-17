import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StudentsGestionComponent } from './students.gestion.component';

describe('StudentsGestionComponent', () => {
  let component: StudentsGestionComponent;
  let fixture: ComponentFixture<StudentsGestionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [StudentsGestionComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(StudentsGestionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
