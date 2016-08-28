      SUBROUTINE TOUR(IK1,N,NB,KOST,INDEX,TOP,KB,INA,KURS)
      IMPLICIT REAL*8 (A-H,O-Z)
      IMPLICIT INTEGER*4 (I-M)
      INTEGER*4    NB(IK1),KOST(IK1),INDEX(IK1),TOP
      INTEGER*4    KB(IK1),INA(IK1)
C - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
C *** DETERMINATION OF AN EULERIAN TOUR
C
      NP      = N+1
      M       = INDEX(NP)
      IF (KURS.LE.0.OR.KURS.GT.N) KURS = 1
      DO 9501 KN = 1,N
         I      = INDEX(KN)-1
         KB(KN) = I
         INA(KN)= I
 9501 CONTINUE
      KN      = KURS
 9503 I       = INA(KN)
 9504 NN      = INDEX(KN+1)-1
 9505 I       = I+1
      IF (I.GT.NN)       GOTO 9540
      KN1     = NB(I)
      IF (KN1.GT.N)      GOTO 9505
      IF (KN1.LT.0)      GOTO 9520
      II      = INA (KN1)
 9510 II      = II+1
      IF (NB(II).NE.KN)  GOTO 9510
      NB(II)  = NP
      II      = KB(KN1)+1
      KB(KN1) = II
      KOST(II)= KN
      INA(KN)   = I
      KN      = KN1
                         GOTO 9503
 9520 KN2     = -KN
      KN1     = -KN1
      II      = INA (KN1)
 9515 II      = II+1
      IF (NB(II).NE.KN2) GOTO 9515
      NB(II)  = NP
      II      = KB(KN1)+1
      KB(KN1) = II
      KOST(II)= KN
      II      = KB(KN)+1
      KB(KN)  = II
      KOST(II)=KN1
                         GOTO 9505
 9540 INA(KN) = M
      DO 9550 KN = 1,N
      I       = INA(KN)
      II      = KB(KN)
      IF (II.GE.INDEX(KN).AND.I.LT.M) GOTO 9504
 9550 CONTINUE
      RETURN
      END
