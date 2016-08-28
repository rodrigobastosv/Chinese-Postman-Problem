      SUBROUTINE KASU (IK1,NB,KOST,INDEX,TOP,KN1,KN2,KANT)
      IMPLICIT INTEGER*4 (I-M)
      INTEGER*4    NB(IK1),KOST(IK1),INDEX(IK1),TOP
C - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
C *** DUPLICATION OF MATCHING EDGES
C
      K1      = KN1
      K2      = KN2
 9100 KANT    = INDEX(K1)
 9120 IF (NB(KANT).EQ.K2) GOTO 9110
      KANT    = KANT+1
                         GOTO 9120
 9110 NB(KANT)  = -K2
      IF (K1.EQ.KN2)     GOTO 9130
      K1      = KN2
      K2      = KN1
                         GOTO 9100
 9130 RETURN
      END
