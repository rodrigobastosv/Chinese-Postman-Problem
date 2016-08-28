      SUBROUTINE PRUEF1 (IK1,N,NB,KOST,INDEX,TOP,BASIS,MEM,KA,KB,
     *                   SM,Y1,Y2,DPLUS,DMINUS,BB)
      IMPLICIT REAL*8 (A,C-H,O-Z)
      IMPLICIT INTEGER*4 (B,I-M)
      INTEGER*4  NB(IK1),KOST(IK1),INDEX(IK1),TOP
      INTEGER*4  BASIS(IK1),MEM(IK1),KA(IK1),KB(IK1),SM(IK1)
      REAL*8     Y1(IK1),Y2(IK1),DPLUS(IK1),DMINUS(IK1)
C - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
C *** SCANNING OF NODE BB
C
      D1        = DPLUS(BB)-Y1(BB)
      KKN       = BB
      KKN1      = SM(BB)
      BB1       = -1
      IF (KKN1.GT.0)  BB1 = BASIS(KKN1)
 9330 I         = INDEX(KKN)
      NN        = INDEX(KKN+1)-1
      Y         = Y2(KKN)
      DO 9300 KANT = I,NN
         KKN2         = NB(KANT)
         BB2          = BASIS(KKN2)
         IF (BB.EQ.BB2)  GOTO 9300
         IF (BB1.EQ.BB2) GOTO 9300
         D2        = DMINUS(BB2)
         YY        = Y1(BB2)+Y2(KKN2)
         D3        = FLOAT(KOST(KANT))
         D3        = D3-Y-YY+D1
         IF (D2.LE.D3)   GOTO 9300
         DMINUS(BB2)= D3
         KA(BB2)   = KKN
         KB(BB2)   = KKN2
 9300 CONTINUE
      KKN          = MEM(KKN)
      IF (KKN.NE.BB)     GOTO 9330
      RETURN
      END
